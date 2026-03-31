// 用户类型枚举
export enum UserType {
  TENANT = 1,    // 租客
  LANDLORD = 2   // 房东
}

// 登录
export interface loginQueryInterface {
  // 手机号码
  phone: string;
  // 短信验证码
  code: string;
  // 用户类型
  userType: UserType;
}
// 获取短信验证码
export interface SmsCodeQueryInterface {
  // 手机号码
  phone: string;
}

// 用户信息
export interface UserInfoInterface {
  // 用户ID（房东ID）
  id: number;
  // 头像
  avatarUrl: string;
  // 用户名
  nickname: string;
  // 用户类型
  userType: UserType;
  // 用户状态
  status: number;
  // 是否为房东
  isLandlord: boolean;
  // 权限列表（房东专用）
  permissions?: string[];
}

// 用户state
export interface UserStateInterface {
  // 用户信息
  userInfo: UserInfoInterface | null;
  // token
  token: string | null;
  // 当前用户类型（字符串格式，用于路由匹配）
  currentUserType: 'TENANT' | 'LANDLORD' | null;
}
