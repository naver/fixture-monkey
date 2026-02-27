import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Fixture Monkey',
  tagline: 'The easiest way to generate fully-customizable, randomly populated instance',
  favicon: 'img/favicon.ico',

  future: {
    v4: true,
  },

  url: 'https://naver.github.io',
  baseUrl: '/fixture-monkey/',

  organizationName: 'naver',
  projectName: 'fixture-monkey',

  onBrokenLinks: 'warn',
  onBrokenAnchors: 'warn',

  markdown: {
    mermaid: true,
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'ko'],
  },

  customFields: {
    fixtureMonkeyVersion: '1.1.15',
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          editUrl: 'https://github.com/naver/fixture-monkey/tree/main/docs-docusaurus/',
          versions: {
            current: {
              label: 'v1.1.x',
            },
            '1.0.x': {
              label: 'v1.0.x',
              banner: 'unmaintained',
            },
            '0.6.x': {
              label: 'v0.6.x',
              banner: 'unmaintained',
            },
          },
          lastVersion: 'current',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themes: ['@docusaurus/theme-mermaid'],

  plugins: [
    [
      '@docusaurus/plugin-client-redirects',
      {
        redirects: [
          // Hugo language-based version URLs → Docusaurus versioned URLs
          {from: '/v1-1-0/docs/get-started/requirements', to: '/docs/get-started/requirements'},
          {from: '/v1-0-0/docs/get-started/requirements', to: '/docs/1.0.x/get-started/requirements'},
          {from: '/v0-6-0/docs/getting-started/java', to: '/docs/0.6.x/getting-started/java'},
        ],
      },
    ],
  ],

  themeConfig: {
    image: 'img/fixtureMonkey.png',
    colorMode: {
      defaultMode: 'dark',
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'Fixture Monkey',
      logo: {
        alt: 'Fixture Monkey Logo',
        src: 'img/fixtureMonkey.png',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Docs',
        },
        {
          type: 'docsVersionDropdown',
          position: 'right',
        },
        {
          type: 'localeDropdown',
          position: 'right',
        },
        {
          href: 'https://github.com/naver/fixture-monkey',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Get Started',
              to: '/docs/get-started/requirements',
            },
            {
              label: 'Generating Objects',
              to: '/docs/generating-objects/fixture-monkey',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/naver/fixture-monkey',
            },
            {
              label: 'Medium',
              href: 'https://medium.com/naver-platform-labs',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'Release Notes',
              to: '/docs/release-notes',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Naver Corp. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'kotlin', 'groovy', 'bash', 'json', 'yaml'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
