import { getToken } from '@/utils/token';

/**
 * Token信息接口
 */
export interface TokenInfo {
  userId: number;
  username: string;
  exp: number; // 过期时间戳（秒）
  iat: number; // 签发时间戳（秒）
  sub: string; // 主题
}

/**
 * Token检查工具类
 * 提供Token的基础检查、解析和验证功能
 */
export class TokenChecker {
  
  /**
   * 检查是否存在Token
   */
  static hasToken(): boolean {
    const token = getToken();
    const hasToken = !!token;
    console.log('🔍 Token存在性检查:', hasToken ? '存在' : '不存在');
    return hasToken;
  }

  /**
   * 获取当前Token
   */
  static getToken(): string | null {
    return getToken();
  }

  /**
   * 检查Token格式是否有效
   * JWT格式应该是三个部分用.分隔
   */
  static isTokenFormatValid(token?: string): boolean {
    const tokenToCheck = token || getToken();
    
    if (!tokenToCheck) {
      console.log('🔍 Token格式检查: Token不存在');
      return false;
    }

    // JWT应该有三个部分：header.payload.signature
    const parts = tokenToCheck.split('.');
    const isValid = parts.length === 3;
    
    console.log('🔍 Token格式检查:', isValid ? '格式有效' : '格式无效', `(${parts.length}个部分)`);
    return isValid;
  }

  /**
   * 解析Token获取用户信息
   * 注意：这里只解析payload部分，不验证签名
   */
  static parseToken(token?: string): TokenInfo | null {
    const tokenToParse = token || getToken();
    
    if (!tokenToParse || !this.isTokenFormatValid(tokenToParse)) {
      console.log('🔍 Token解析失败: Token不存在或格式无效');
      return null;
    }

    try {
      // 获取payload部分（第二部分）
      const payload = tokenToParse.split('.')[1];
      
      // Base64解码
      const decoded = JSON.parse(atob(payload));
      
      const tokenInfo: TokenInfo = {
        userId: decoded.userId,
        username: decoded.username,
        exp: decoded.exp,
        iat: decoded.iat,
        sub: decoded.sub
      };

      console.log('🔍 Token解析成功:', {
        userId: tokenInfo.userId,
        username: tokenInfo.username,
        过期时间: new Date(tokenInfo.exp * 1000).toLocaleString(),
        签发时间: new Date(tokenInfo.iat * 1000).toLocaleString()
      });

      return tokenInfo;
    } catch (error) {
      console.error('🔍 Token解析失败:', error);
      return null;
    }
  }

  /**
   * 检查Token是否已过期
   */
  static isTokenExpired(token?: string): boolean {
    const tokenInfo = this.parseToken(token);
    
    if (!tokenInfo) {
      console.log('🔍 Token过期检查: 无法解析Token，视为已过期');
      return true;
    }

    const now = Math.floor(Date.now() / 1000); // 当前时间戳（秒）
    const isExpired = tokenInfo.exp < now;
    
    if (isExpired) {
      const expiredTime = new Date(tokenInfo.exp * 1000).toLocaleString();
      console.log('🔍 Token已过期:', `过期时间 ${expiredTime}`);
    } else {
      const remainingMinutes = Math.floor((tokenInfo.exp - now) / 60);
      console.log('🔍 Token未过期:', `剩余 ${remainingMinutes} 分钟`);
    }

    return isExpired;
  }

  /**
   * 获取Token过期时间
   */
  static getExpirationTime(token?: string): Date | null {
    const tokenInfo = this.parseToken(token);
    
    if (!tokenInfo) {
      return null;
    }

    return new Date(tokenInfo.exp * 1000);
  }

  /**
   * 获取Token剩余有效时间（分钟）
   */
  static getRemainingMinutes(token?: string): number {
    const tokenInfo = this.parseToken(token);
    
    if (!tokenInfo) {
      return 0;
    }

    const now = Math.floor(Date.now() / 1000);
    const remainingSeconds = tokenInfo.exp - now;
    
    return Math.max(0, Math.floor(remainingSeconds / 60));
  }

  /**
   * 检查Token是否即将过期（默认30分钟内）
   */
  static isTokenExpiringSoon(thresholdMinutes: number = 30, token?: string): boolean {
    const remainingMinutes = this.getRemainingMinutes(token);
    const isExpiringSoon = remainingMinutes <= thresholdMinutes && remainingMinutes > 0;
    
    if (isExpiringSoon) {
      console.log('🔍 Token即将过期:', `剩余 ${remainingMinutes} 分钟`);
    }
    
    return isExpiringSoon;
  }

  /**
   * 获取Token中的用户ID
   */
  static getUserId(token?: string): number | null {
    const tokenInfo = this.parseToken(token);
    return tokenInfo?.userId || null;
  }

  /**
   * 获取Token中的用户名
   */
  static getUsername(token?: string): string | null {
    const tokenInfo = this.parseToken(token);
    return tokenInfo?.username || null;
  }

  /**
   * 强制检查当前Token状态（用于调试）
   */
  static forceCheckTokenStatus(): void {
    console.log('🔍 开始强制检查token状态');
    
    const hasToken = this.hasToken();
    console.log('🔍 当前token:', hasToken ? '存在' : '不存在');
    
    if (hasToken) {
      const isValidFormat = this.isTokenFormatValid();
      console.log('🔍 token格式:', isValidFormat ? '有效' : '无效');
      
      if (isValidFormat) {
        const tokenInfo = this.parseToken();
        if (tokenInfo) {
          console.log('🔍 用户信息:', tokenInfo.username ? '存在' : '不存在');
          
          const isExpired = this.isTokenExpired();
          console.log('🔍 过期状态:', isExpired ? '已过期' : '未过期');
          
          if (!isExpired) {
            const remainingMinutes = this.getRemainingMinutes();
            console.log('🔍 剩余时间:', `${remainingMinutes} 分钟`);
            
            const isExpiringSoon = this.isTokenExpiringSoon();
            console.log('🔍 即将过期:', isExpiringSoon ? '是' : '否');
          }
        }
      }
    }
    
    console.log('🔍 token检查通过');
  }
}