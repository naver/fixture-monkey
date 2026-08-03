const {
	rspack,
	getSwcJsMinimizerOptions,
	getLightningCssMinimizerOptions,
	getBrowserslistQueries,
} = require('@docusaurus/faster');

/**
 * Docusaurus configures the Rspack JS minimizer without `extractComments`, so
 * third-party license banners are dropped from the client bundle. The webpack
 * path keeps them because terser-webpack-plugin extracts comments by default.
 *
 * This plugin rebuilds both Rspack minimizers with the same options Docusaurus
 * uses, adding `extractComments` so the `*.LICENSE.txt` files are emitted again.
 * Keep in sync with @docusaurus/bundler `getRspackMinimizers()`.
 */
module.exports = function preserveLicenseCommentsPlugin() {
	return {
		name: 'preserve-license-comments',
		configureWebpack(config, isServer, {currentBundler}) {
			if (currentBundler.name !== 'rspack' || !config.optimization?.minimize) {
				return {};
			}

			const swcJsMinimizerOptions = getSwcJsMinimizerOptions();

			return {
				mergeStrategy: {'optimization.minimizer': 'replace'},
				optimization: {
					minimizer: [
						new rspack.SwcJsMinimizerRspackPlugin({
							minimizerOptions: {
								minify: true,
								ecma: swcJsMinimizerOptions.ecma,
								...swcJsMinimizerOptions,
							},
							extractComments: true,
						}),
						new rspack.LightningCssMinimizerRspackPlugin({
							minimizerOptions: {
								...getLightningCssMinimizerOptions(),
								targets: getBrowserslistQueries({
									isServer: false,
									bundlerName: 'rspack',
								}),
							},
						}),
					],
				},
			};
		},
	};
};
