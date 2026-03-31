import { createApp } from "vue";
import { store } from "./store";
// normalize.css
import "normalize.css/normalize.css";
// 引入 Vant 样式
import "vant/lib/index.css";
// 引入样式
import "./styles/index";
// svg icon
import "virtual:svg-icons-register";

import App from "./App.vue";
import router from "./router";
// 引入指令
import { setupDirectives } from "@/directives";
// 引入应用启动检查器
import { AppStartupChecker } from "@/utils/appStartupChecker";

const app = createApp(App);
app.use(store);
app.use(router);
// 注册指令
setupDirectives(app);

// 在应用挂载前执行启动检查
AppStartupChecker.performStartupChecks().then(() => {
  console.log('🚀 应用启动检查完成，开始挂载应用');
  app.mount("#app");
}).catch((error) => {
  console.error('🚀 应用启动检查失败，但仍然挂载应用:', error);
  // 即使启动检查失败，也要挂载应用，确保用户能够使用
  app.mount("#app");
});
