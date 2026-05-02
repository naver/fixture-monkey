const path = require('path');

module.exports = function sourceCodeLoaderPlugin(context) {
	return {
		name: 'source-code-loader',
		configureWebpack() {
			return {
				resolve: {
					alias: {
						'@examples-java': path.resolve(
							context.siteDir,
							'../fixture-monkey-tests/doc-examples/java-examples/src/test/java/com/navercorp/fixturemonkey/docs'
						),
						'@examples-kotlin': path.resolve(
							context.siteDir,
							'../fixture-monkey-tests/doc-examples/kotlin-examples/src/test/kotlin/com/navercorp/fixturemonkey/docs'
						),
					},
				},
				module: {
					rules: [
						{
							test: /\.(java|kt)$/,
							use: [path.resolve(__dirname, 'loader.js')],
							type: 'javascript/auto',
						},
					],
				},
			};
		},
	};
};
