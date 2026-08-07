const fs = require('fs');
const path = require('path');

const SOURCE_DIR = path.resolve(__dirname, '..', '..', 'docs', 'agent-guide');
const ROUTE_BASE = '/docs/agent-guide';

/**
 * Docusaurus compiles MDX to React and emits only HTML, so the agent guide is
 * reachable by people but not as plain text. AI agents reading these pages get
 * the rendered page including navbar, sidebar and footer, which buries the
 * content they came for.
 *
 * This plugin copies the agent-guide sources into the build output as `.md`
 * alongside the generated HTML, so `/docs/agent-guide/writing-tests` serves the
 * page and `/docs/agent-guide/writing-tests.md` serves the source. There is one
 * source of truth: `docs/agent-guide/*.md`.
 *
 * Front matter is replaced by its title as an H1, and relative links are
 * rewritten to absolute URLs so they still resolve outside the site.
 */
module.exports = function emitAgentMarkdownPlugin() {
	return {
		name: 'emit-agent-markdown',
		async postBuild({siteConfig, outDir}) {
			if (!fs.existsSync(SOURCE_DIR)) {
				return;
			}

			const siteUrl = siteConfig.url + siteConfig.baseUrl.replace(/\/$/, '');
			const targetDir = path.join(outDir, 'docs', 'agent-guide');
			await fs.promises.mkdir(targetDir, {recursive: true});

			const files = (await fs.promises.readdir(SOURCE_DIR)).filter((it) => it.endsWith('.md'));

			await Promise.all(
				files.map(async (file) => {
					const raw = await fs.promises.readFile(path.join(SOURCE_DIR, file), 'utf-8');
					const emitted = rewriteLinks(stripFrontMatter(raw), siteUrl);
					await fs.promises.writeFile(path.join(targetDir, file), emitted, 'utf-8');
				}),
			);
		},
	};
};

/**
 * Replaces the front matter block with its `title` as an H1, so the emitted
 * file keeps the heading the rendered page shows.
 */
function stripFrontMatter(content) {
	const match = /^---\n([\s\S]*?)\n---\n/.exec(content);
	if (!match) {
		return content;
	}

	const title = /^title:\s*["']?(.*?)["']?\s*$/m.exec(match[1]);
	const heading = title ? `# ${title[1]}\n\n` : '';
	return heading + content.slice(match[0].length).replace(/^\n+/, '');
}

/**
 * Rewrites relative markdown links to absolute site URLs. A reader that fetched
 * the raw file has no page context, so `../plugins/kotlin-plugin/features` would
 * otherwise resolve against nothing.
 *
 * Every source file sits directly in the agent-guide directory, so relative
 * targets always resolve against ROUTE_BASE — including `index.md`, whose own
 * route is that directory.
 */
function rewriteLinks(content, siteUrl) {
	return content.replace(/\]\((\.[^)\s]*)\)/g, (_match, target) => {
		const [pathPart, hash = ''] = target.split('#');
		const resolved = path.posix.resolve(ROUTE_BASE, pathPart || '.');
		return `](${siteUrl}${resolved}${hash ? `#${hash}` : ''})`;
	});
}
