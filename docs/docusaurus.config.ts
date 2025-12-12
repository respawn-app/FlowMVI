import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const description = `Architecture Framework for Kotlin. Reuse every line of code. Handle all errors automatically. No boilerplate. Analytics, metrics, debugging in 3 lines. 50+ features.`;

const config: Config = {
    title: "FlowMVI",
    tagline: "Simplify Complexity.",
    favicon: "/favicon.ico",
    url: "https://opensource.respawn.pro",
    baseUrl: "/FlowMVI/",
    organizationName: "respawn-app",
    projectName: "FlowMVI",
    onBrokenLinks: "throw",
    onBrokenMarkdownLinks: "throw",
    onDuplicateRoutes: "throw",
    trailingSlash: false,
    markdown: { mermaid: true },
    i18n: { defaultLocale: "en", locales: ["en"] },
    stylesheets: [
        "https://fonts.googleapis.com/css2?family=Comfortaa:wght@400;500;600;700&family=Montserrat+Alternates:wght@500;600;700&display=swap",
    ],
    headTags: [
        { tagName: "link", attributes: { rel: "preconnect", href: "https://fonts.googleapis.com" } },
        { tagName: "link", attributes: { rel: "preconnect", href: "https://fonts.gstatic.com", crossorigin: "anonymous" } },
        { tagName: "link", attributes: { rel: "preload", href: "https://cdn.jsdelivr.net/gh/githubnext/monaspace@v1.000/fonts/MonaspaceNeon-Regular.woff", as: "font", type: "font/woff" } },
    ],
    presets: [
        { name: "classic", options: { docs: { breadcrumbs: false, sidebarCollapsed: false, sidebarPath: "./sidebars.ts", routeBasePath: "/", editUrl: "https://github.com/respawn-app/FlowMVI/blob/master/docs/" }, blog: false, pages: false, theme: { customCss: "./src/css/custom.css" } }, gtag: { trackingID: "G-NRB9ZFKNGN", apiKey: "" } },
    ],
    themes: ['@docusaurus/theme-mermaid'],
    themeConfig: {
        colorMode: { defaultMode: "dark", respectPrefersColorScheme: true },
        metadata: [
            { name: "theme-color", content: "#00d46a" },
            { name: "og:type", content: "website" },
            { name: "og:site_name", content: "FlowMVI" },
            { name: "twitter:card", content: "https://opensource.respawn.pro/FlowMVI/banner.png" },
            { name: "twitter:title", content: "FlowMVI" },
            { name: "twitter:description", content: description },
            { name: "og:description", content: description },
            { name: "description", content: description },
        ],
        image: "/banner.png",
        algolia: { contextualSearch: false, appId: "", apiKey: "", indexName: "opensource-respawn" },
        docs: { sidebar: { hideable: true, autoCollapseCategories: false } },
    },
    plugins: [
        [{ name: "@docusaurus/plugin-pwa", options: { offlineModeActivationStrategies: ["appInstalled", "standalone", "queryString", "saveData"], pwaHead: [
            { tagName: "link", rel: "icon", href: "/favicon.ico" },
            { tagName: "link", rel: "manifest", href: "manifest.json" },
            { tagName: "meta", name: "theme-color", content: "#00d46a" },
            { tagName: "link", rel: "apple-touch-icon", href: "/apple-touch-icon.png" },
        ]} }],
    ],
};

export default config;