const path = require('path');

module.exports = function sourceWithPathLoader(source) {
	const repoRoot = path.resolve(this.rootContext, '..');
	const relativePath = path.relative(repoRoot, this.resourcePath).replace(/\\/g, '/');
	const isDev = process.env.NODE_ENV === 'development';

	let code = `const s = new String(${JSON.stringify(source)});\n`;
	code += `s.__sourcePath = ${JSON.stringify(relativePath)};\n`;

	if (isDev) {
		code += `s.__absolutePath = ${JSON.stringify(this.resourcePath)};\n`;
	}

	code += `export default s;\n`;
	return code;
};
