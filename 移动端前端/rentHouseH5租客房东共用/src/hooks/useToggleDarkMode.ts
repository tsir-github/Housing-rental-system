import { ref } from 'vue';

const isDark = ref(false);

/**
 * 暗黑模式切换hook
 */
export function useDarkMode() {
  return isDark.value;
}

/**
 * 切换暗黑模式
 */
export function toggleDarkMode() {
  isDark.value = !isDark.value;
}