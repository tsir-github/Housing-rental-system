import { defineStore } from 'pinia';

interface CachedViewState {
  cachedViewList: string[];
}

export const useCachedViewStore = defineStore({
  id: 'cached-view',
  state: (): CachedViewState => ({
    cachedViewList: []
  }),
  actions: {
    addCachedView(viewName: string) {
      if (!this.cachedViewList.includes(viewName)) {
        this.cachedViewList.push(viewName);
      }
    },
    removeCachedView(viewName: string) {
      const index = this.cachedViewList.indexOf(viewName);
      if (index > -1) {
        this.cachedViewList.splice(index, 1);
      }
    },
    clearCachedViews() {
      this.cachedViewList = [];
    },
    // 添加缺失的方法
    delAllCachedViews() {
      this.cachedViewList = [];
    }
  }
});

export function useCachedViewStoreHook() {
  return useCachedViewStore();
}