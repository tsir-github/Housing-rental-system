// 高德地图hooks
import AMapLoader from "@amap/amap-jsapi-loader";
import {
  AMAP_MAP_KEY,
  AMAP_MAP_SECRET_KEY
} from "@/config/config";
import { onUnmounted, ref } from "vue";

export const useMap = ({ domId }: { domId?: string } = {}) => {
  const AMap = ref();
  const map = ref();
  
  // 网络检测函数
  const checkNetworkConnection = async (): Promise<boolean> => {
    try {
      // 尝试访问一个简单的网络资源
      const response = await fetch('https://www.baidu.com/favicon.ico', {
        method: 'HEAD',
        mode: 'no-cors',
        cache: 'no-cache'
      });
      return true;
    } catch (error) {
      console.warn("网络连接检测失败:", error);
      return navigator.onLine; // 回退到浏览器的网络状态
    }
  };
  
  async function initMap(retryCount = 3) {
    for (let attempt = 1; attempt <= retryCount; attempt++) {
      try {
        console.log(`第${attempt}次尝试加载高德地图API...`);
        
        // 检查网络连接
        const isOnline = await checkNetworkConnection();
        if (!isOnline) {
          throw new Error("网络连接不可用，请检查网络设置");
        }
        
        // 使用正确的安全密钥配置
        (window as any)._AMapSecurityConfig = {
          securityJsCode: AMAP_MAP_SECRET_KEY
        };
        
        console.log("正在加载高德地图API，密钥:", AMAP_MAP_KEY);
        
        // 设置超时时间
        const loadPromise = AMapLoader.load({
          key: AMAP_MAP_KEY, // 申请好的Web端开发者Key，首次调用 load 时必填
          version: "2.0", // 指定要加载的 JS API 的版本，缺省时默认为 1.4.15
          plugins: ['AMap.Geocoder'] // 需要使用的的插件列表，包含地理编码插件
        });
        
        // 添加超时控制
        const timeoutPromise = new Promise((_, reject) => {
          setTimeout(() => reject(new Error('地图API加载超时')), 10000);
        });
        
        AMap.value = await Promise.race([loadPromise, timeoutPromise]);
        
        // 验证AMap是否正确加载
        if (!AMap.value || !AMap.value.Map) {
          throw new Error("高德地图API加载失败");
        }
        
        map.value = domId && new AMap.value.Map(domId);
        console.log("高德地图API加载成功");
        return; // 成功后退出重试循环
        
      } catch (error) {
        console.error(`第${attempt}次地图加载失败:`, error);
        
        if (attempt === retryCount) {
          // 最后一次尝试失败，抛出错误
          throw new Error(`地图加载失败，已重试${retryCount}次: ${error}`);
        }
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, 2000 * attempt));
      }
    }
  }
  
  function destroyMap() {
    map.value && map.value.destroy();
  }
  
  onUnmounted(() => {
    destroyMap();
  });
  
  return {
    map,
    AMap,
    initMap
  };
};
