import { fileURLToPath, URL } from "node:url";
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import Components from "unplugin-vue-components/vite";
import { VantResolver } from "unplugin-vue-components/resolvers";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
import * as path from "path";
import mockDevServerPlugin from "vite-plugin-mock-dev-server";
import vueSetupExtend from "vite-plugin-vue-setup-extend";
import viteCompression from "vite-plugin-compression";
import { createHtmlPlugin } from "vite-plugin-html";

// 当前工作目录路径
const root: string = process.cwd();

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 环境变量
  const env = loadEnv(mode, root, "");
  return {
    base: "/",
    plugins: [
      vue(),
      vueJsx(),
      mockDevServerPlugin(),
      // vant 组件自动按需引入
      Components({
        resolvers: [VantResolver({
          importStyle: false // 禁用自动导入样式，手动导入
        })]
      }),
      // svg icon
      createSvgIconsPlugin({
        // 指定图标文件夹
        iconDirs: [path.resolve(root, "src/icons/svg")],
        // 指定 symbolId 格式
        symbolId: "icon-[dir]-[name]",
        //svgo额外配置，具体配置参考https://github.com/svg/svgo
        svgoOptions: {
          plugins: [
            // 去除所有svg的"class", "data-name", "fill", "stroke"属性
            {
              name: "removeAttrs",
              params: { attrs: ["class", "data-name", "fill", "stroke"] }
            }
          ]
        }
      }),
      // 允许 setup 语法糖上添加组件名属性
      vueSetupExtend(),
      // 生产环境 gzip 压缩资源
      viteCompression(),
      // 注入模板数据
      createHtmlPlugin({
        inject: {
          data: {
            ENABLE_ERUDA: env.VITE_ENABLE_ERUDA || "false"
          }
        }
      })
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
        "@common": fileURLToPath(new URL("./src/components/common", import.meta.url)),
        "@tenant": fileURLToPath(new URL("./src/components/tenant", import.meta.url)),
        "@landlord": fileURLToPath(new URL("./src/components/landlord", import.meta.url)),
        "@api": fileURLToPath(new URL("./src/api", import.meta.url)),
        "@views": fileURLToPath(new URL("./src/views", import.meta.url))
      }
    },
    server: {
      host: "localhost",
      port: 5173,
      open: false, // 禁用自动打开浏览器
      proxy: {
        "/app": {
          target: "http://localhost:8081",
          changeOrigin: true,
          secure: false,
          configure: (proxy, options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              // 确保自定义请求头被正确转发
              if (req.headers['access-token']) {
                proxyReq.setHeader('access-token', req.headers['access-token']);
              }
            });
          }
        },
        "/api": {
          target: "http://localhost:8081",
          changeOrigin: true,
          secure: false,
          configure: (proxy, options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              // 确保自定义请求头被正确转发
              if (req.headers['access-token']) {
                proxyReq.setHeader('access-token', req.headers['access-token']);
              }
            });
          }
        }
      }
    },
    build: {
      rollupOptions: {
        output: {
          chunkFileNames: "static/js/[name]-[hash].js",
          entryFileNames: "static/js/[name]-[hash].js",
          assetFileNames: "static/[ext]/[name]-[hash].[ext]"
        }
      }
    }
  };
});
