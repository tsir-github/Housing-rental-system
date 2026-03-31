/**
 * UnauthorizedHandler 测试文件
 * 用于验证401错误处理器的基本功能
 */

import { UnauthorizedHandler } from './unauthorizedHandler';

// 模拟测试函数
export function testUnauthorizedHandler() {
    console.log('🧪 开始测试 UnauthorizedHandler');

    // 测试1: 检查初始状态
    console.log('🧪 测试1: 检查初始状态');
    console.log('是否正在处理401错误:', UnauthorizedHandler.isHandling401());

    // 测试2: 测试错误描述获取
    console.log('🧪 测试2: 测试错误描述获取');
    const descriptions = [
        '401_TOKEN_EXPIRED',
        '401_TOKEN_INVALID',
        '401_NO_TOKEN',
        '401_SERVER_ERROR',
        '401_UNKNOWN'
    ];

    descriptions.forEach(reason => {
        const desc = UnauthorizedHandler.getErrorDescription(reason);
        console.log(`错误原因 ${reason} 的描述: ${desc}`);
    });

    // 测试3: 测试重置状态功能
    console.log('🧪 测试3: 测试重置状态功能');
    UnauthorizedHandler.resetHandlingState();
    console.log('重置后是否正在处理401错误:', UnauthorizedHandler.isHandling401());

    console.log('🧪 UnauthorizedHandler 基础功能测试完成');
}

// 模拟401错误对象
export function createMock401Error(message: string = 'Unauthorized') {
    return {
        response: {
            status: 401,
            data: {
                message: message,
                code: 401
            }
        },
        config: {
            url: '/api/test'
        },
        message: 'Request failed with status code 401'
    };
}

// 在开发环境下可以调用这个函数进行测试
if (process.env.NODE_ENV === 'development') {
    // 延迟执行，确保其他模块已加载
    setTimeout(() => {
        try {
            testUnauthorizedHandler();
        } catch (error) {
            console.error('🧪 测试执行失败:', error);
        }
    }, 1000);
}