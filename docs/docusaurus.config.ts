import { themes as prismThemes } from 'prism-react-renderer';
import type { Config } from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {

    title: 'FlowMVI',
    tagline: 'Dinosaurs are cool',
    favicon: '/favicon.ico',

    url: 'https://opensource.respawn.pro',
    baseUrl: '/flowmvi/',
    organizationName: 'respawn-app',
    projectName: 'FlowMVI',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'throw',
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },
    presets: [
        [
            'classic',
            {
                docs: {
                    sidebarPath: './sidebars.ts',
                    routeBasePath: '/',
                    editUrl: 'https://github.com/respawn-app/flowmvi/tree/main/docs',
                },
                blog: false,
                pages: false,
                theme: {
                    customCss: './src/css/custom.css',
                },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    image: 'static/banner.png',
      docs: {
          sidebar: {
              hideable: true,
              autoCollapseCategories: false,
        }
    },
    navbar: {
      title: 'FlowMVI',
      logo: {
        alt: 'Logo',
        src: '/icon.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'sidebar',
          position: 'left',
          label: undefined,
        },
        {
          href: 'https://opensource.respawn.pro/FlowMVI/javadocs/index.html',
          label: 'API Docs',
          position: 'right',
        },
      ],
    },
    footer: {
    style: 'dark',
      copyright: `Copyright © ${new Date().getFullYear()} Respawn Open Source Team.`,
    },
    prism: {
      theme: prismThemes.oneLight,
      darkTheme: prismThemes.oneDark,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
