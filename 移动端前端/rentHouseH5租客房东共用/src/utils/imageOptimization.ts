/**
 * 图片压缩配置
 */
export interface ImageCompressConfig {
  maxWidth?: number;
  maxHeight?: number;
  quality?: number;
  format?: 'jpeg' | 'png' | 'webp';
  enableResize?: boolean;
}

/**
 * 懒加载配置
 */
export interface LazyLoadConfig {
  rootMargin?: string;
  threshold?: number;
  placeholder?: string;
  errorImage?: string;
  fadeIn?: boolean;
  retryCount?: number;
}

/**
 * 图片优化工具类
 */
export class ImageOptimization {
  private static instance: ImageOptimization;
  private observer: IntersectionObserver | null = null;
  private loadingImages = new Set<HTMLImageElement>();
  private loadedImages = new WeakSet<HTMLImageElement>();

  private constructor() {
    this.setupIntersectionObserver();
  }

  static getInstance(): ImageOptimization {
    if (!ImageOptimization.instance) {
      ImageOptimization.instance = new ImageOptimization();
    }
    return ImageOptimization.instance;
  }

  /**
   * 设置交叉观察器
   */
  private setupIntersectionObserver() {
    if ('IntersectionObserver' in window) {
      this.observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              const img = entry.target as HTMLImageElement;
              this.loadImage(img);
              this.observer?.unobserve(img);
            }
          });
        },
        {
          rootMargin: '50px',
          threshold: 0.1
        }
      );
    }
  }

  /**
   * 压缩图片
   */
  async compressImage(
    file: File | Blob,
    config: ImageCompressConfig = {}
  ): Promise<Blob> {
    const {
      maxWidth = 1920,
      maxHeight = 1080,
      quality = 0.8,
      format = 'jpeg',
      enableResize = true
    } = config;

    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      const img = new Image();

      img.onload = () => {
        try {
          let { width, height } = img;

          // 计算新尺寸
          if (enableResize) {
            const ratio = Math.min(maxWidth / width, maxHeight / height);
            if (ratio < 1) {
              width *= ratio;
              height *= ratio;
            }
          }

          canvas.width = width;
          canvas.height = height;

          // 绘制图片
          ctx?.drawImage(img, 0, 0, width, height);

          // 转换为Blob
          canvas.toBlob(
            (blob) => {
              if (blob) {
                resolve(blob);
              } else {
                reject(new Error('Failed to compress image'));
              }
            },
            `image/${format}`,
            quality
          );
        } catch (error) {
          reject(error);
        }
      };

      img.onerror = () => {
        reject(new Error('Failed to load image'));
      };

      img.src = URL.createObjectURL(file);
    });
  }

  /**
   * 批量压缩图片
   */
  async compressImages(
    files: (File | Blob)[],
    config: ImageCompressConfig = {}
  ): Promise<Blob[]> {
    const promises = files.map(file => this.compressImage(file, config));
    return Promise.all(promises);
  }

  /**
   * 懒加载图片
   */
  lazyLoad(
    img: HTMLImageElement,
    config: LazyLoadConfig = {}
  ): () => void {
    const {
      placeholder = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwIiB5PSI1MCIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Yqg6L295LitLi4uPC90ZXh0Pjwvc3ZnPg==',
      errorImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwIiB5PSI1MCIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Yqg6L295aSx6LSlPC90ZXh0Pjwvc3ZnPg==',
      fadeIn = true,
      retryCount = 3
    } = config;

    // 设置占位图
    if (!img.src && placeholder) {
      img.src = placeholder;
    }

    // 存储原始src
    const originalSrc = img.dataset.src || img.src;
    if (!originalSrc) return () => {};

    // 如果已经加载过，直接返回
    if (this.loadedImages.has(img)) {
      return () => {};
    }

    // 添加到观察器
    if (this.observer) {
      this.observer.observe(img);
    } else {
      // 降级处理：直接加载
      this.loadImage(img);
    }

    // 返回清理函数
    return () => {
      if (this.observer) {
        this.observer.unobserve(img);
      }
      this.loadingImages.delete(img);
    };
  }

  /**
   * 加载图片
   */
  private async loadImage(img: HTMLImageElement) {
    if (this.loadingImages.has(img) || this.loadedImages.has(img)) {
      return;
    }

    this.loadingImages.add(img);

    const originalSrc = img.dataset.src || img.getAttribute('data-original-src');
    if (!originalSrc) {
      this.loadingImages.delete(img);
      return;
    }

    try {
      // 预加载图片
      const newImg = new Image();
      
      await new Promise((resolve, reject) => {
        newImg.onload = resolve;
        newImg.onerror = reject;
        newImg.src = originalSrc;
      });

      // 设置图片源
      img.src = originalSrc;
      
      // 添加淡入效果
      if (img.dataset.fadeIn === 'true') {
        img.style.opacity = '0';
        img.style.transition = 'opacity 0.3s ease-in-out';
        
        // 强制重绘
        img.offsetHeight;
        
        img.style.opacity = '1';
      }

      // 标记为已加载
      this.loadedImages.add(img);
      
      // 触发自定义事件
      img.dispatchEvent(new CustomEvent('lazyloaded', {
        detail: { src: originalSrc }
      }));

    } catch (error) {
      console.warn('Failed to load image:', originalSrc, error);
      
      // 设置错误图片
      const errorImage = img.dataset.errorImage || 
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwIiB5PSI1MCIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Yqg6L295aSx6LSlPC90ZXh0Pjwvc3ZnPg==';
      
      img.src = errorImage;
      
      // 触发错误事件
      img.dispatchEvent(new CustomEvent('lazyerror', {
        detail: { src: originalSrc, error }
      }));
    } finally {
      this.loadingImages.delete(img);
    }
  }

  /**
   * 预加载图片
   */
  preloadImages(urls: string[]): Promise<void[]> {
    const promises = urls.map(url => {
      return new Promise<void>((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve();
        img.onerror = () => reject(new Error(`Failed to preload: ${url}`));
        img.src = url;
      });
    });

    return Promise.all(promises);
  }

  /**
   * 获取图片信息
   */
  async getImageInfo(file: File | Blob): Promise<{
    width: number;
    height: number;
    size: number;
    type: string;
  }> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      
      img.onload = () => {
        resolve({
          width: img.naturalWidth,
          height: img.naturalHeight,
          size: file.size,
          type: file.type
        });
      };
      
      img.onerror = () => {
        reject(new Error('Failed to load image'));
      };
      
      img.src = URL.createObjectURL(file);
    });
  }

  /**
   * 转换图片格式
   */
  async convertFormat(
    file: File | Blob,
    targetFormat: 'jpeg' | 'png' | 'webp',
    quality = 0.8
  ): Promise<Blob> {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      const img = new Image();

      img.onload = () => {
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        
        ctx?.drawImage(img, 0, 0);
        
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob);
            } else {
              reject(new Error('Failed to convert image format'));
            }
          },
          `image/${targetFormat}`,
          quality
        );
      };

      img.onerror = () => {
        reject(new Error('Failed to load image'));
      };

      img.src = URL.createObjectURL(file);
    });
  }

  /**
   * 生成缩略图
   */
  async generateThumbnail(
    file: File | Blob,
    width: number,
    height: number,
    quality = 0.8
  ): Promise<Blob> {
    return this.compressImage(file, {
      maxWidth: width,
      maxHeight: height,
      quality,
      enableResize: true
    });
  }

  /**
   * 销毁实例
   */
  destroy() {
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }
    this.loadingImages.clear();
  }
}

/**
 * 创建图片优化实例
 */
export const imageOptimization = ImageOptimization.getInstance();

/**
 * 图片优化组合式函数
 */
export function useImageOptimization() {
  const optimization = ImageOptimization.getInstance();

  const compressImage = (file: File | Blob, config?: ImageCompressConfig) => 
    optimization.compressImage(file, config);

  const compressImages = (files: (File | Blob)[], config?: ImageCompressConfig) => 
    optimization.compressImages(files, config);

  const lazyLoad = (img: HTMLImageElement, config?: LazyLoadConfig) => 
    optimization.lazyLoad(img, config);

  const preloadImages = (urls: string[]) => 
    optimization.preloadImages(urls);

  const getImageInfo = (file: File | Blob) => 
    optimization.getImageInfo(file);

  const convertFormat = (file: File | Blob, format: 'jpeg' | 'png' | 'webp', quality?: number) => 
    optimization.convertFormat(file, format, quality);

  const generateThumbnail = (file: File | Blob, width: number, height: number, quality?: number) => 
    optimization.generateThumbnail(file, width, height, quality);

  return {
    compressImage,
    compressImages,
    lazyLoad,
    preloadImages,
    getImageInfo,
    convertFormat,
    generateThumbnail
  };
}

/**
 * 懒加载指令
 */
export function createLazyLoadDirective() {
  return {
    mounted(el: HTMLImageElement, binding: any) {
      const config = binding.value || {};
      const cleanup = imageOptimization.lazyLoad(el, config);
      
      // 存储清理函数
      (el as any)._lazyLoadCleanup = cleanup;
    },
    
    unmounted(el: HTMLImageElement) {
      if ((el as any)._lazyLoadCleanup) {
        (el as any)._lazyLoadCleanup();
      }
    }
  };
}

/**
 * 图片压缩工具函数
 */
export async function compressImageFile(
  file: File,
  options: {
    maxSizeMB?: number;
    maxWidthOrHeight?: number;
    useWebWorker?: boolean;
  } = {}
): Promise<File> {
  const {
    maxSizeMB = 1,
    maxWidthOrHeight = 1920,
    useWebWorker = false
  } = options;

  // 如果文件已经很小，直接返回
  if (file.size <= maxSizeMB * 1024 * 1024) {
    return file;
  }

  const compressedBlob = await imageOptimization.compressImage(file, {
    maxWidth: maxWidthOrHeight,
    maxHeight: maxWidthOrHeight,
    quality: 0.8
  });

  // 创建新的File对象
  return new File([compressedBlob], file.name, {
    type: compressedBlob.type,
    lastModified: Date.now()
  });
}