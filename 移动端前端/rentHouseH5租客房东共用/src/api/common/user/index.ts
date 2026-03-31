import http from "@/utils/http";
import type {
  loginQueryInterface,
  SmsCodeQueryInterface,
  UserInfoInterface
} from "@/api/common/user/types";

/**
 * @description 登录
 * @param params
 */
export function login(params: loginQueryInterface) {
  return http.post<string>(`/app/login`, params);
}

/**
 * @description 获取短信验证码
 * @param params
 */
export function getSmsCode(params: SmsCodeQueryInterface) {
  return http.get(`/app/login/getCode?phone=${params.phone}`);
}

/**
 * @description 获取用户信息
 */
export function getUserInfo() {
  console.log('getUserInfo调用 - 准备发送请求');
  console.log('getUserInfo调用 - localStorage中的token:', localStorage.getItem('ACCESS_TOKEN'));
  
  return http.get<UserInfoInterface>(`/app/info`);
}
