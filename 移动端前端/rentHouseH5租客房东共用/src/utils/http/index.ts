import axios from "axios";
import type {
  AxiosInstance,
  AxiosError,
  AxiosRequestConfig,
  AxiosResponse
} from "axios";
import { showToast, showFailToast } from "vant";
import { ResultEnum } from "@/enums/requestEnum";
import NProgress from "../progress";
import "vant/es/toast/style";
import { getToken } from "@/utils/token";
import type { ResultData } from "@/utils/http/type";
import { AuthHandler } from "@/utils/authHandler";

export const service: AxiosInstance = axios.create({
  // 使用环境变量配置baseURL，开发环境走代理
  baseURL: import.meta.env.VITE_APP_BASE_URL || "",
  timeout: 60000  // 增加到60秒，适应AI接口的处理时间
});

/**
 * @description: 请求拦截器
 * @returns {*}
 */
service.interceptors.request.use(
  config => {
    if ((config as HttpConfigProps).showNProgress) {
      NProgress.start();
    }
    
    const token = getToken();
    console.log('🔍 HTTP请求拦截器 - URL:', config.url);
    
    // 检查是否是不需要token的请求（登录、获取验证码等）
    const publicUrls = ['/app/login', '/app/login/getCode'];
    const isPublicUrl = publicUrls.some(url => config.url?.includes(url));
    
    if (isPublicUrl) {
      console.log('🔍 公开接口，无需token验证');
      return config;
    }
    
    // 对于需要认证的接口，设置token
    if (token) {
      config.headers["access-token"] = token;
      console.log('🔍 已设置access-token请求头');
    } else {
      console.log('🔍 需要认证的接口但token不存在，将在响应拦截器中处理');
    }
    
    return config;
  },
  (error: AxiosError) => {
    console.error('🔍 请求拦截器错误:', error);
    showFailToast(error.message);
    return Promise.reject(error);
  }
);

/**
 * @description: 响应拦截器
 * @returns {*}
 */
service.interceptors.response.use(
  (response: AxiosResponse) => {
    NProgress.done();
    const { data } = response;
    
    // 检查业务层面的token过期
    if (ResultEnum.EXPIRE.includes(data.code)) {
      console.log('🔍 检测到业务层面token过期，状态码:', data.code);
      AuthHandler.handleTokenExpired(false); // 不显示对话框，直接跳转
      return Promise.reject(data);
    }

    if (data.code && data.code !== ResultEnum.SUCCESS) {
      showToast(data.message || ResultEnum.ERRMESSAGE);
      return Promise.reject(data);
    }
    return data;
  },
  async (error: AxiosError) => {
    NProgress.done();
    
    const status = error.response?.status;
    let message = "";
    
    switch (status) {
      case 400:
        message = "请求错误";
        break;
      case 401:
        console.log('🔍 检测到HTTP 401错误，清理认证信息并跳转登录');
        // 简化401处理：直接清理数据并跳转
        AuthHandler.handleTokenExpired(false);
        return Promise.reject(error);
        
      case 403:
        message = "权限不足";
        break;
        
      case 404:
        message = `请求地址出错: ${error.response?.config?.url}`;
        break;
      case 408:
        message = "请求超时";
        break;
      case 500:
        message = "服务器内部错误";
        break;
      case 501:
        message = "服务未实现";
        break;
      case 502:
        message = "网关错误";
        break;
      case 503:
        message = "服务不可用";
        break;
      case 504:
        message = "网关超时";
        break;
      case 505:
        message = "HTTP版本不受支持";
        break;
      default:
        if (!navigator.onLine) {
          message = "网络连接已断开，请检查网络后重试";
        } else if (error.code === 'NETWORK_ERROR' || error.message.includes('Network Error')) {
          message = "网络连接故障";
        } else {
          message = "网络连接故障";
        }
    }

    // 显示错误提示（除了401，因为401会自动跳转登录）
    if (status !== 401 && message) {
      showFailToast(message);
    }
    
    return Promise.reject(error);
  }
);

/**
 * @description: 导出封装的请求方法
 * @returns {*}
 */
interface HttpConfigProps extends AxiosRequestConfig {
  showNProgress?: boolean;
}

const http = {
  get<T>(
    url: string,
    params?: object,
    config?: HttpConfigProps
  ): Promise<ResultData<T>> {
    console.log('HTTP GET - URL:', url);
    console.log('HTTP GET - params:', params);
    
    const requestConfig = {
      ...config,
      params: params
    };
    
    console.log('HTTP GET - 最终请求配置:', requestConfig);
    return service.get(url, requestConfig);
  },

  post<T>(
    url: string,
    data?: object,
    config?: HttpConfigProps
  ): Promise<ResultData<T>> {
    return service.post(url, data, config);
  },

  put<T>(
    url: string,
    data?: object,
    config?: HttpConfigProps
  ): Promise<ResultData<T>> {
    return service.put(url, data, config);
  },

  delete<T>(
    url: string,
    data?: object,
    config?: AxiosRequestConfig
  ): Promise<ResultData<T>> {
    return service.delete(url, { data, ...config });
  }
};

export default http;