import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

// 配置NProgress
NProgress.configure({
  showSpinner: false, // 不显示加载图标
  minimum: 0.2,
  easing: 'ease',
  speed: 500
});

export default NProgress;