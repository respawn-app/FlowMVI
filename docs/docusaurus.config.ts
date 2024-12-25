import { themes as prismThemes } from 'prism-react-renderer';
import type { Config } from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {

    title: 'FlowMVI',
    tagline: 'Simplify Complexity.',
    favicon: '/favicon.ico',
    url: 'https://opensource.respawn.pro',
    baseUrl: '/flowmvi/',
    organizationName: 'respawn-app',
    projectName: 'FlowMVI',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'throw',
    onDuplicateRoutes: 'throw',
    trailingSlash: false,
    markdown: {
        mermaid: true,
    },
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },
    presets: [
        [
            'classic',
            {
                docs: {
                    sidebarCollapsed: false,
                    sidebarPath: './sidebars.ts',
                    routeBasePath: '/',
                    editUrl: 'https://github.com/respawn-app/flowmvi/tree/main/docs',
                },
                blog: false,
                pages: false,
                theme: {
                    customCss: './src/css/custom.css',
                },
                gtag: {
                    trackingID: "G-NRB9ZFKNGN",
                }
            } satisfies Preset.Options,
        ],
    ],
    themeConfig: {
        colorMode: {
            defaultMode: 'dark',
            respectPrefersColorScheme: true,
        },
        metadata: [
            { name: "theme-color", content: "#00d46a" },
            { name: "og:type", content: "website" },
            { name: "og:site_name", content: "FlowMVI" },
            { name: "twitter:card", content: "https://opensource.respawn.pro/FlowMVI/banner.png" },
            { name: "twitter:title", content: "FlowMVI" },
            { name: "twitter:description", content: "Architecture Framework for Kotlin. Reuse every line of code. Handle all errors automatically. No boilerplate. Analytics, metrics, debugging in 3 lines. 50+ features." },
            { name: "og:description", content: "Architecture Framework for Kotlin. Reuse every line of code. Handle all errors automatically. No boilerplate. Analytics, metrics, debugging in 3 lines. 50+ features." },
            { name: "description", content: "Architecture Framework for Kotlin. Reuse every line of code. Handle all errors automatically. No boilerplate. Analytics, metrics, debugging in 3 lines. 50+ features." },
        ],
        image: 'static/banner.png',
        docs: {
            sidebar: {
                hideable: true,
                autoCollapseCategories: false,
            },
        },
        navbar: {
            title: 'FlowMVI',
            hideOnScroll: true,
            style: "dark",
            logo: {
                alt: 'Logo',
                src: '/icon.svg',
            },
            items: [
                {
                    href: "/",
                    label: `© ${new Date().getFullYear()} Respawn OSS`,
                    position: 'right',
                },
                {
                    href: 'https://opensource.respawn.pro/FlowMVI/javadocs/index.html',
                    label: 'API Docs',
                    position: 'right',
                },
                {
                    href: 'https://github.com/respawn-app/FlowMVI',
                    label: undefined,
                    className: 'header-github-link',
                    position: 'right',
                },
            ],
        },
        footer: undefined,
        prism: {
            theme: prismThemes.oneLight,
            darkTheme: prismThemes.oneDark,
            additionalLanguages: [
                'java',
                'kotlin',
                'bash',
                'diff',
                'json',
                'toml',
                'yaml',
                'gradle',
                'groovy',
                `properties`,
            ],
            magicComments: [
                {
                    className: 'theme-code-block-highlighted-line',
                    line: 'highlight-next-line',
                    block: { start: 'highlight-start', end: 'highlight-end' },
                },
                {
                    className: 'code-block-error-line',
                    line: 'This will error',
                },
            ],
        },
    } satisfies Preset.ThemeConfig,
    plugins: [
        [
            '@docusaurus/plugin-pwa',
            {
                offlineModeActivationStrategies: [
                    'appInstalled',
                    'standalone',
                    'queryString',
                ],
                pwaHead: [
                    {
                        tagName: 'link',
                        rel: 'icon',
                        href: '/img/icon.svg',
                    },
                    {
                        tagName: 'link',
                        rel: 'manifest',
                        href: '/manifest.json',
                    },
                    {
                        tagName: 'meta',
                        name: 'theme-color',
                        content: '#00d46a',
                    },
                    {
                        tagName: 'link',
                        rel: 'apple-touch-icon',
                        href: '/img/apple-touch-icon.png',
                    },
                ],
            },
        ],
    ],
};

export default config;