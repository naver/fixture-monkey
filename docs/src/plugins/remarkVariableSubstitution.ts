import type {Plugin} from 'unified';
import type {Root} from 'mdast';
import {visit} from 'unist-util-visit';

interface Options {
	variables: Record<string, string>;
}

const VARIABLE_PATTERN = /\{\{(\w+)\}\}/g;

function replaceVariables(text: string, variables: Record<string, string>): string {
	return text.replace(VARIABLE_PATTERN, (match, name) => {
		return variables[name] ?? match;
	});
}

const remarkVariableSubstitution: Plugin<[Options], Root> = (options) => {
	const {variables} = options;

	return (tree) => {
		visit(tree, (node: any) => {
			if (node.type === 'text' || node.type === 'inlineCode') {
				node.value = replaceVariables(node.value, variables);
			}

			if (node.type === 'code' && typeof node.value === 'string') {
				node.value = replaceVariables(node.value, variables);
			}

			if (node.type === 'link' && typeof node.url === 'string') {
				node.url = replaceVariables(node.url, variables);
			}
		});
	};
};

export default remarkVariableSubstitution;
