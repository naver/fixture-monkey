#!/usr/bin/env node

/**
 * Hugo → Docusaurus Migration Script
 *
 * Converts Hugo (Doks theme) documentation to Docusaurus format.
 * Handles: file routing, front matter, tabpane/tab, alert, mermaid,
 * fixture-monkey-version, ref/relref, _index.md → _category_.json
 */

import fs from 'fs';
import path from 'path';

const HUGO_CONTENT_DIR = path.resolve(import.meta.dirname, '../../docs/content');
const DOCUSAURUS_DIR = path.resolve(import.meta.dirname, '..');

// Version-specific fixture-monkey versions
const VERSION_MAP = {
  'v1.1.x': '1.1.15',
  'v1.0.x': '1.0.20',
  'v0.6.x': '0.6.12',
};

// Hugo config version value (used by {{< param "version" >}} and {{< param version >}})
const PARAM_VERSION_MAP = {
  'v1.1.x': '1.1.15',
  'v1.0.x': '1.0.20',
  'v0.6.x': '0.6.12',
};

// Mapping: Hugo source dir → Docusaurus destination dir
const ROUTE_MAP = [
  { src: 'v1.1.x/docs', dest: 'docs', version: 'v1.1.x' },
  { src: 'v1.1.x/release-notes', dest: 'docs', version: 'v1.1.x', isReleaseNotes: true },
  { src: 'v1.1.x-kor/docs', dest: 'i18n/ko/docusaurus-plugin-content-docs/current', version: 'v1.1.x' },
  { src: 'v1.1.x-kor/release-notes', dest: 'i18n/ko/docusaurus-plugin-content-docs/current', version: 'v1.1.x', isReleaseNotes: true },
  { src: 'v1.0.x/docs', dest: 'versioned_docs/version-1.0.x', version: 'v1.0.x' },
  { src: 'v1.0.x/release-notes', dest: 'versioned_docs/version-1.0.x', version: 'v1.0.x', isReleaseNotes: true },
  { src: 'v1.0.x-kor/docs', dest: 'i18n/ko/docusaurus-plugin-content-docs/version-1.0.x', version: 'v1.0.x' },
  { src: 'v1.0.x-kor/release-notes', dest: 'i18n/ko/docusaurus-plugin-content-docs/version-1.0.x', version: 'v1.0.x', isReleaseNotes: true },
  { src: 'v0.6.x/docs', dest: 'versioned_docs/version-0.6.x', version: 'v0.6.x' },
];

// Icon → admonition type mapping
const ICON_TO_ADMONITION = {
  '💡': 'tip',
  '⭐': 'tip',
  '⚠️': 'warning',
  '⚠': 'warning',
  '🚨': 'danger',
  '📌': 'info',
  '📖': 'note',
  '📘': 'note',
};

// Alert color → admonition type mapping (v0.6.x)
const COLOR_TO_ADMONITION = {
  'primary': 'tip',
  'secondary': 'note',
  'success': 'tip',
  'danger': 'danger',
  'warning': 'warning',
  'info': 'info',
};

let stats = {
  filesProcessed: 0,
  tabsConverted: 0,
  alertsConverted: 0,
  mermaidsConverted: 0,
  versionsReplaced: 0,
  refsConverted: 0,
  categoryJsonCreated: 0,
  mdxFiles: 0,
};

// ─── Front Matter ───────────────────────────────────────────────────

function parseFrontMatter(content) {
  const match = content.match(/^---\n([\s\S]*?)\n---\n?([\s\S]*)$/);
  if (!match) return { frontMatter: {}, body: content, rawFM: '' };

  const rawFM = match[1];
  const body = match[2];
  const fm = {};

  // Simple YAML parser for our subset
  const lines = rawFM.split('\n');
  for (const line of lines) {
    const kv = line.match(/^(\w[\w-]*):\s*(.*)$/);
    if (kv) {
      let val = kv[2].trim();
      if (val === '[]') val = [];
      else if (val === 'true') val = true;
      else if (val === 'false') val = false;
      else if (/^\d+$/.test(val)) val = parseInt(val, 10);
      else if (val.startsWith('"') && val.endsWith('"')) val = val.slice(1, -1);
      fm[kv[1]] = val;
    }
  }

  return { frontMatter: fm, body, rawFM };
}

function buildDocusaurusFrontMatter(fm, isIndex, dirName) {
  const result = {};

  if (fm.title) result.title = fm.title;

  // weight → sidebar_position
  if (fm.weight !== undefined) {
    result.sidebar_position = fm.weight;
  }

  // For _index.md files that have content, they become category index pages
  if (isIndex) {
    // will be handled separately via _category_.json
    return result;
  }

  const lines = ['---'];
  for (const [key, val] of Object.entries(result)) {
    if (typeof val === 'string') {
      // Escape quotes in title
      if (val.includes('"') || val.includes(':') || val.includes('#')) {
        lines.push(`${key}: "${val.replace(/"/g, '\\"')}"`);
      } else {
        lines.push(`${key}: "${val}"`);
      }
    } else {
      lines.push(`${key}: ${val}`);
    }
  }
  lines.push('---');
  return lines.join('\n');
}

// ─── Shortcode Converters ───────────────────────────────────────────

function convertTabpanes(content) {
  let needsImport = false;

  // Process tabpane blocks
  // Regex to match {{< tabpane ... >}} ... {{< /tabpane>}} or {{< /tabpane >}}
  const tabpaneRegex = /\{\{<\s*tabpane\s*([^}]*?)>\}\}([\s\S]*?)\{\{<\s*\/\s*tabpane\s*>?\}\}/g;

  content = content.replace(tabpaneRegex, (match, tabpaneAttrs, innerContent) => {
    needsImport = true;
    stats.tabsConverted++;

    // Extract default lang from tabpane (e.g. {{< tabpane lang="java" >}})
    const defaultLangMatch = tabpaneAttrs.match(/lang\s*=\s*"([^"]*)"/);
    const defaultLang = defaultLangMatch ? defaultLangMatch[1] : null;

    // Parse individual tabs
    const tabs = [];
    // Match {{< tab header="..." lang="..." >}} ... {{< /tab >}} (also handles {{<tab without space)
    const tabRegex = /\{\{<\s*tab\s+([^}]*?)>?\}\}([\s\S]*?)\{\{<\s*\/\s*tab\s*>\}\}/g;
    let tabMatch;

    while ((tabMatch = tabRegex.exec(innerContent)) !== null) {
      const attrs = tabMatch[1];
      const tabContent = tabMatch[2];

      // Extract header
      const headerMatch = attrs.match(/header\s*=\s*"([^"]*)"/);
      const header = headerMatch ? headerMatch[1] : 'Tab';

      // Extract lang
      const langMatch = attrs.match(/lang\s*=\s*"([^"]*)"/);
      const lang = langMatch ? langMatch[1] : defaultLang;

      // Determine the language for code block
      let codeLang = lang;
      if (!codeLang) {
        // Infer from header
        const h = header.toLowerCase();
        if (h === 'java') codeLang = 'java';
        else if (h === 'kotlin') codeLang = 'kotlin';
        else if (h === 'kotlin exp') codeLang = 'kotlin';
        else if (h === 'groovy') codeLang = 'groovy';
        else if (h.includes('gradle') && h.includes('groovy')) codeLang = 'groovy';
        else if (h.includes('gradle') && h.includes('kotlin')) codeLang = 'kotlin';
        else if (h === 'gradle') codeLang = 'groovy';
        else if (h === 'maven') codeLang = 'xml';
        else if (h === 'xml') codeLang = 'xml';
        else if (h === 'json') codeLang = 'json';
        else if (h === 'yaml') codeLang = 'yaml';
        else if (h === 'bash' || h === 'shell') codeLang = 'bash';
      }

      // Determine value from header
      const value = header.toLowerCase().replace(/\s+/g, '-');

      tabs.push({ header, value, codeLang, content: tabContent });
    }

    if (tabs.length === 0) return match; // no tabs found, return original

    // Build Tabs/TabItem output
    let result = '\n<Tabs groupId="language">\n';
    for (const tab of tabs) {
      result += `<TabItem value="${tab.value}" label="${tab.header}">\n`;

      let trimmedContent = tab.content.trim();

      if (trimmedContent && tab.codeLang) {
        // Wrap in fenced code block
        result += `\n\`\`\`${tab.codeLang}\n${trimmedContent}\n\`\`\`\n`;
      } else if (trimmedContent) {
        result += `\n${trimmedContent}\n`;
      }

      result += '\n</TabItem>\n';
    }
    result += '</Tabs>\n';

    return result;
  });

  return { content, needsImport };
}

function convertAlerts(content) {
  let converted = content;

  // Pattern 1: Self-closing alerts (v1.0.x, v1.1.x)
  // {{< alert icon="💡" text="message" />}}
  converted = converted.replace(
    /\{\{<\s*alert\s+((?:icon|text|context|color|title)\s*=\s*"[^"]*"\s*)+\/>\}\}/g,
    (match) => {
      stats.alertsConverted++;
      const iconMatch = match.match(/icon\s*=\s*"([^"]*)"/);
      const textMatch = match.match(/text\s*=\s*"([^"]*)"/);
      const contextMatch = match.match(/context\s*=\s*"([^"]*)"/);

      const icon = iconMatch ? iconMatch[1] : '';
      const text = textMatch ? textMatch[1] : '';
      const context = contextMatch ? contextMatch[1] : '';

      let admonitionType = 'note';
      if (icon && ICON_TO_ADMONITION[icon]) {
        admonitionType = ICON_TO_ADMONITION[icon];
      } else if (context && COLOR_TO_ADMONITION[context]) {
        admonitionType = COLOR_TO_ADMONITION[context];
      }

      return `:::${admonitionType}\n${text}\n:::`;
    }
  );

  // Pattern 2: Block alerts with title (v1.1.x)
  // {{< alert icon="💡" title="Important" >}}content{{</ alert>}} or {{< /alert >}}
  converted = converted.replace(
    /\{\{<\s*alert\s+((?:(?:icon|text|context|color|title)\s*=\s*"[^"]*"\s*)*)\s*>\}\}([\s\S]*?)\{\{<\s*\/?\s*alert\s*>?\}\}/g,
    (match, attrs, innerContent) => {
      stats.alertsConverted++;
      const iconMatch = attrs.match(/icon\s*=\s*"([^"]*)"/);
      const titleMatch = attrs.match(/title\s*=\s*"([^"]*)"/);
      const contextMatch = attrs.match(/context\s*=\s*"([^"]*)"/);
      const colorMatch = attrs.match(/color\s*=\s*"([^"]*)"/);

      const icon = iconMatch ? iconMatch[1] : '';
      const title = titleMatch ? titleMatch[1] : '';
      const context = contextMatch ? contextMatch[1] : '';
      const color = colorMatch ? colorMatch[1] : '';

      let admonitionType = 'note';
      if (icon && ICON_TO_ADMONITION[icon]) {
        admonitionType = ICON_TO_ADMONITION[icon];
      } else if (context && COLOR_TO_ADMONITION[context]) {
        admonitionType = COLOR_TO_ADMONITION[context];
      } else if (color && COLOR_TO_ADMONITION[color]) {
        admonitionType = COLOR_TO_ADMONITION[color];
      }

      const titleStr = title ? `[${title}]` : '';
      const trimmed = innerContent.trim();

      return `:::${admonitionType}${titleStr}\n${trimmed}\n:::`;
    }
  );

  return converted;
}

function convertMermaid(content) {
  // {{< mermaid >}} or {{< mermaid class="..." >}} ... {{< /mermaid >}} or {{</ mermaid >}}
  return content.replace(
    /\{\{<\s*mermaid\s*(?:class="[^"]*"\s*)?>\}\}([\s\S]*?)\{\{<\s*\/?\s*mermaid\s*>\}\}/g,
    (match, inner) => {
      stats.mermaidsConverted++;
      return '```mermaid\n' + inner.trim() + '\n```';
    }
  );
}

function convertVersionShortcode(content, version) {
  const versionValue = VERSION_MAP[version] || '1.1.15';

  // {{< fixture-monkey-version >}}
  let result = content.replace(/\{\{<\s*fixture-monkey-version\s*>\}\}/g, () => {
    stats.versionsReplaced++;
    return versionValue;
  });

  // {{< param "version" >}} and {{< param version >}}
  result = result.replace(/\{\{<\s*param\s+"?version"?\s*>\}\}/g, () => {
    stats.versionsReplaced++;
    return PARAM_VERSION_MAP[version] || versionValue;
  });

  return result;
}

function convertRefs(content) {
  // {{< ref "/docs/..." >}} → relative path
  let result = content.replace(
    /\{\{<\s*ref\s+"([^"]+)"\s*>\}\}/g,
    (match, refPath) => {
      stats.refsConverted++;
      // Convert /docs/customizing-objects/path-expressions → ../customizing-objects/path-expressions
      // Remove leading /docs/ and convert to relative
      let cleaned = refPath.replace(/^\/docs\//, '../');
      return cleaned;
    }
  );

  // {{< relref "..." >}} → relative path
  result = result.replace(
    /\{\{<\s*relref\s+"([^"]+)"\s*>\}\}/g,
    (match, refPath) => {
      stats.refsConverted++;
      return refPath;
    }
  );

  return result;
}

// ─── File Processing ────────────────────────────────────────────────

function normalizeDirectoryName(name) {
  // v0.6.x has spaces in directory names like "Getting started"
  return name.toLowerCase().replace(/\s+/g, '-');
}

function processFile(srcPath, destDir, version, isReleaseNotes) {
  const content = fs.readFileSync(srcPath, 'utf-8');
  const fileName = path.basename(srcPath);
  const relativePath = path.relative(path.join(HUGO_CONTENT_DIR), srcPath);

  // Skip the top-level version _index.md (e.g., v1.1.x/_index.md)
  if (fileName === '_index.md') {
    const dirName = path.basename(path.dirname(srcPath));
    const { frontMatter, body } = parseFrontMatter(content);

    // If this is a directory index, create _category_.json
    const position = frontMatter.weight || 99;
    const label = frontMatter.title || dirName;

    // Normalize directory name for destination
    const normalizedDirName = normalizeDirectoryName(dirName);
    const categoryDestDir = isReleaseNotes
      ? destDir
      : path.join(destDir, normalizedDirName !== dirName ? normalizedDirName : '');

    // If body has significant content, also create an index.md
    const bodyTrimmed = body.trim();
    if (bodyTrimmed.length > 0) {
      // This _index.md has content — create both _category_.json and index.md
      const ensuredDir = isReleaseNotes ? destDir : path.join(destDir);
      fs.mkdirSync(path.join(DOCUSAURUS_DIR, ensuredDir), { recursive: true });

      // For release notes, write as release-notes.md directly
      if (isReleaseNotes) {
        let processedBody = bodyTrimmed;
        processedBody = convertVersionShortcode(processedBody, version);
        processedBody = convertRefs(processedBody);

        const fmStr = `---\ntitle: "${label}"\nsidebar_position: ${position}\n---`;
        const destPath = path.join(DOCUSAURUS_DIR, destDir, 'release-notes.md');
        fs.writeFileSync(destPath, `${fmStr}\n\n${processedBody}\n`);
        stats.filesProcessed++;
        return;
      }

      // Create _category_.json
      const categoryJson = {
        label,
        position,
        collapsible: true,
        collapsed: true,
      };
      fs.writeFileSync(
        path.join(DOCUSAURUS_DIR, ensuredDir, '_category_.json'),
        JSON.stringify(categoryJson, null, 2)
      );
      stats.categoryJsonCreated++;

      // Also write the content as index.md
      let processedBody = processContent(bodyTrimmed, version);
      const hasTabImport = processedBody.needsImport;
      processedBody = processedBody.content;

      const ext = hasTabImport ? 'mdx' : 'md';
      if (hasTabImport) stats.mdxFiles++;

      let fileContent = `---\ntitle: "${label}"\nsidebar_position: ${position}\n---\n\n`;
      if (hasTabImport) {
        fileContent += `import Tabs from '@theme/Tabs';\nimport TabItem from '@theme/TabItem';\n\n`;
      }
      fileContent += processedBody + '\n';

      fs.writeFileSync(
        path.join(DOCUSAURUS_DIR, ensuredDir, `index.${ext}`),
        fileContent
      );
      stats.filesProcessed++;
    } else {
      // Only create _category_.json (no content)
      if (!isReleaseNotes) {
        fs.mkdirSync(path.join(DOCUSAURUS_DIR, destDir), { recursive: true });
        const categoryJson = {
          label,
          position,
          collapsible: true,
          collapsed: true,
        };
        fs.writeFileSync(
          path.join(DOCUSAURUS_DIR, destDir, '_category_.json'),
          JSON.stringify(categoryJson, null, 2)
        );
        stats.categoryJsonCreated++;
      }
    }
    return;
  }

  // Regular markdown file
  const { frontMatter, body, rawFM } = parseFrontMatter(content);
  let processedResult = processContent(body, version);
  let processedBody = processedResult.content;
  const hasTabImport = processedResult.needsImport;

  // Build front matter
  const fmLines = ['---'];
  if (frontMatter.title) {
    const title = frontMatter.title.replace(/"/g, '\\"');
    fmLines.push(`title: "${title}"`);
  }
  if (frontMatter.weight !== undefined) {
    fmLines.push(`sidebar_position: ${frontMatter.weight}`);
  }
  fmLines.push('---');

  const ext = hasTabImport ? 'mdx' : 'md';
  if (hasTabImport) stats.mdxFiles++;

  let fileContent = fmLines.join('\n') + '\n\n';
  if (hasTabImport) {
    fileContent += `import Tabs from '@theme/Tabs';\nimport TabItem from '@theme/TabItem';\n\n`;
  }
  fileContent += processedBody + '\n';

  // Normalize filename (handle spaces, especially v0.6.x)
  let destFileName = fileName.replace(/\s+/g, '-').toLowerCase();
  // Change extension if needed
  if (ext === 'mdx') {
    destFileName = destFileName.replace(/\.md$/, '.mdx');
  }

  const destPath = path.join(DOCUSAURUS_DIR, destDir, destFileName);
  fs.mkdirSync(path.dirname(destPath), { recursive: true });
  fs.writeFileSync(destPath, fileContent);
  stats.filesProcessed++;
}

function processContent(body, version) {
  let content = body;

  // 1. Convert tabpanes first (most complex)
  const tabResult = convertTabpanes(content);
  content = tabResult.content;

  // 2. Convert alerts
  content = convertAlerts(content);

  // 3. Convert mermaid
  content = convertMermaid(content);

  // 4. Convert version shortcodes
  content = convertVersionShortcode(content, version);

  // 5. Convert ref/relref
  content = convertRefs(content);

  // 6. Clean up any remaining Hugo shortcodes
  // {{< blocks/... >}} etc
  content = content.replace(/\{\{<\s*\/?\s*blocks\/\w+[^>]*>\}\}/g, '');

  return { content, needsImport: tabResult.needsImport };
}

// ─── Directory Walker ───────────────────────────────────────────────

function walkDir(dir, callback, relativeTo) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walkDir(fullPath, callback, relativeTo);
    } else if (entry.name.endsWith('.md')) {
      callback(fullPath);
    }
  }
}

function computeDestSubPath(srcFilePath, srcBaseDir) {
  // Get relative path from srcBase
  const rel = path.relative(srcBaseDir, srcFilePath);
  const parts = rel.split(path.sep);

  // Normalize directory names (spaces → hyphens, lowercase)
  const normalized = parts.map((part, i) => {
    if (i === parts.length - 1) return part; // keep filename as-is for now
    return normalizeDirectoryName(part);
  });

  return normalized.join(path.sep);
}

// ─── Main ───────────────────────────────────────────────────────────

function main() {
  console.log('🚀 Starting Hugo → Docusaurus migration...\n');

  for (const route of ROUTE_MAP) {
    const srcDir = path.join(HUGO_CONTENT_DIR, route.src);
    if (!fs.existsSync(srcDir)) {
      console.log(`⚠️  Skipping ${route.src} (not found)`);
      continue;
    }

    console.log(`📁 Processing ${route.src} → ${route.dest}`);

    walkDir(srcDir, (filePath) => {
      // Compute relative destination path
      const relFromSrc = path.relative(srcDir, filePath);
      const parts = relFromSrc.split(path.sep);

      // Normalize directory names
      const normalizedParts = parts.map((part, i) => {
        if (i === parts.length - 1) return part; // filename handled in processFile
        return normalizeDirectoryName(part);
      });

      const destSubDir = normalizedParts.slice(0, -1).join(path.sep);
      const fullDestDir = path.join(route.dest, destSubDir);

      // Ensure destination directory exists
      fs.mkdirSync(path.join(DOCUSAURUS_DIR, fullDestDir), { recursive: true });

      processFile(filePath, fullDestDir, route.version, route.isReleaseNotes);
    });
  }

  // Create versions.json
  const versionsJson = ['1.0.x', '0.6.x'];
  fs.writeFileSync(
    path.join(DOCUSAURUS_DIR, 'versions.json'),
    JSON.stringify(versionsJson, null, 2)
  );

  // Create versioned sidebars
  const versionedSidebarsDir = path.join(DOCUSAURUS_DIR, 'versioned_sidebars');
  fs.mkdirSync(versionedSidebarsDir, { recursive: true });

  for (const ver of versionsJson) {
    const sidebarContent = {
      docsSidebar: [{ type: 'autogenerated', dirName: '.' }],
    };
    fs.writeFileSync(
      path.join(versionedSidebarsDir, `version-${ver}-sidebars.json`),
      JSON.stringify(sidebarContent, null, 2)
    );
  }

  console.log('\n✅ Migration complete!\n');
  console.log('📊 Statistics:');
  console.log(`   Files processed: ${stats.filesProcessed}`);
  console.log(`   Tabs converted: ${stats.tabsConverted}`);
  console.log(`   Alerts converted: ${stats.alertsConverted}`);
  console.log(`   Mermaid blocks converted: ${stats.mermaidsConverted}`);
  console.log(`   Version placeholders replaced: ${stats.versionsReplaced}`);
  console.log(`   Refs converted: ${stats.refsConverted}`);
  console.log(`   Category JSONs created: ${stats.categoryJsonCreated}`);
  console.log(`   MDX files (with tabs): ${stats.mdxFiles}`);
}

main();
