/**
 * AppStartupChecker 测试文件
 * 用于验证应用启动检查器的基本功能
 */

import { AppStartupChecker } from './appStartupChecker';

// 模拟测试函数
export function testAppStartupChecker() {
    console.log('🧪 开始测试 AppStartupChecker');

    // 测试1: 检查初始化状态
    console.log('🧪 测试1: 检查初始化状态');
    const status = AppStartupChecker.getInitializationStatus();
    console.log('初始化状态:', status);

    // 测试2: 重置初始化状态
    console.log('🧪 测试2: 重置初始化状态');
    AppStartupChecker.resetInitializationState();
    const resetStatus = AppStartupChecker.getInitializationStatus();
    console.log('重置后状态:', resetStatus);

    // 测试3: 手动触发Token检查
    console.log('🧪 测试3: 手动触发Token检查');
    try {
        AppStartupChecker.manualTokenCheck();
        console.log('✅ 手动Token检查方法可调用');
    } catch (error) {
        console.error('❌ 手动Token检查失败:', error);
    }

    // 测试4: 清理功能
    console.log('🧪 测试4: 清理功能');
    try {
        AppStartupChecker.cleanup();
        console.log('✅ 清理方法可调用');
    } catch (error) {
        console.error('❌ 清理方法失败:', error);
    }

    console.log('🧪 AppStartupChecker 基础功能测试完成');
}

// 模拟启动检查流程
export function testStartupFlow() {
    console.log('🧪 测试启动检查流程');

    // 设置一些测试数据
    localStorage.setItem('ACCESS_TOKEN', 'test-token-123');
    localStorage.setItem('FORM_DATA_test', JSON.stringify({
        data: { test: 'value' },
        timestamp: Date.now() - 20 * 60 * 1000 // 20分钟前的数据，应该被清理
    }));

    console.log('🧪 设置测试数据完成');
    console.log('启动检查前 ACCESS_TOKEN:', localStorage.getItem('ACCESS_TOKEN'));
    console.log('启动检查前 FORM_DATA_test:', localStorage.getItem('FORM_DATA_test'));

    // 执行启动检查（注意：这会显示对话框，在实际测试中需要小心）
    // AppStartupChecker.performStartupChecks();

    console.log('🧪 启动检查流程测试准备完成（实际执行需要用户交互）');
}

// 测试定期检查功能
export function testPeriodicChecks() {
    console.log('🧪 测试定期检查功能');

    // 由于定期检查是在内部设置的，我们只能测试相关的状态
    const testCases = [
        {
            name: '检查初始化状态',
            test: () => AppStartupChecker.getInitializationStatus()
        },
        {
            name: '手动Token检查',
            test: () => AppStartupChecker.manualTokenCheck()
        }
    ];

    testCases.forEach(({ name, test }) => {
        console.log(`🧪 测试 ${name}`);
        try {
            const result = test();
            console.log(`✅ ${name} 执行成功:`, result);
        } catch (error) {
            console.error(`❌ ${name} 测试失败:`, error);
        }
    });
}

// 测试错误处理
export function testErrorHandling() {
    console.log('🧪 测试错误处理');

    // 模拟各种错误情况
    const errorScenarios = [
        {
            name: '无效Token格式',
            setup: () => localStorage.setItem('ACCESS_TOKEN', 'invalid-token')
        },
        {
            name: '过期Token',
            setup: () => {
                // 创建一个已过期的JWT token（这里只是示例，实际需要有效的JWT格式）
                const expiredToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MDAwMDAwMDB9.invalid';
                localStorage.setItem('ACCESS_TOKEN', expiredToken);
            }
        },
        {
            name: '空Token',
            setup: () => localStorage.setItem('ACCESS_TOKEN', '')
        },
        {
            name: '无Token',
            setup: () => localStorage.removeItem('ACCESS_TOKEN')
        }
    ];

    errorScenarios.forEach(({ name, setup }) => {
        console.log(`🧪 测试错误场景: ${name}`);
        try {
            setup();
            console.log(`✅ ${name} 场景设置完成`);
            // 实际的错误处理会在启动检查中进行
        } catch (error) {
            console.error(`❌ ${name} 场景设置失败:`, error);
        }
    });
}

// 在开发环境下可以调用这个函数进行测试
if (process.env.NODE_ENV === 'development') {
    // 延迟执行，确保其他模块已加载
    setTimeout(() => {
        try {
            testAppStartupChecker();
            testStartupFlow();
            testPeriodicChecks();
            testErrorHandling();
        } catch (error) {
            console.error('🧪 测试执行失败:', error);
        }
    }, 2000); // 延迟2秒，让应用启动检查先完成
}