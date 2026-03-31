<template>
  <div class="skeleton-loader" :class="{ animated: animated }">
    <!-- 卡片骨架屏 -->
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-header">
        <div class="skeleton-avatar" v-if="showAvatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-subtitle" v-if="showSubtitle"></div>
        </div>
      </div>
      <div class="skeleton-body">
        <div 
          v-for="n in rows" 
          :key="n" 
          class="skeleton-row"
          :style="{ width: getRowWidth(n) }"
        ></div>
      </div>
      <div class="skeleton-actions" v-if="showActions">
        <div class="skeleton-button"></div>
        <div class="skeleton-button"></div>
      </div>
    </div>

    <!-- 列表骨架屏 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div 
        v-for="n in count" 
        :key="n" 
        class="skeleton-list-item"
      >
        <div class="skeleton-avatar" v-if="showAvatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-subtitle" v-if="showSubtitle"></div>
        </div>
        <div class="skeleton-action" v-if="showActions"></div>
      </div>
    </div>

    <!-- 表格骨架屏 -->
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-header">
        <div 
          v-for="n in columns" 
          :key="n" 
          class="skeleton-table-cell"
        ></div>
      </div>
      <div 
        v-for="n in rows" 
        :key="n" 
        class="skeleton-table-row"
      >
        <div 
          v-for="m in columns" 
          :key="m" 
          class="skeleton-table-cell"
        ></div>
      </div>
    </div>

    <!-- 文章骨架屏 -->
    <div v-else-if="type === 'article'" class="skeleton-article">
      <div class="skeleton-article-title"></div>
      <div class="skeleton-article-meta">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-meta-info">
          <div class="skeleton-author"></div>
          <div class="skeleton-date"></div>
        </div>
      </div>
      <div class="skeleton-article-content">
        <div 
          v-for="n in paragraphs" 
          :key="n" 
          class="skeleton-paragraph"
        >
          <div 
            v-for="m in getParagraphLines(n)" 
            :key="m" 
            class="skeleton-line"
            :style="{ width: getLineWidth(m, getParagraphLines(n)) }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 图片骨架屏 -->
    <div v-else-if="type === 'image'" class="skeleton-image">
      <div 
        class="skeleton-image-placeholder"
        :style="{ 
          width: imageWidth, 
          height: imageHeight,
          aspectRatio: aspectRatio 
        }"
      ></div>
      <div class="skeleton-image-caption" v-if="showCaption"></div>
    </div>

    <!-- 自定义骨架屏 -->
    <div v-else-if="type === 'custom'" class="skeleton-custom">
      <slot name="skeleton">
        <div class="skeleton-default">
          <div 
            v-for="n in rows" 
            :key="n" 
            class="skeleton-row"
            :style="{ width: getRowWidth(n) }"
          ></div>
        </div>
      </slot>
    </div>

    <!-- 默认骨架屏 -->
    <div v-else class="skeleton-default">
      <div 
        v-for="n in rows" 
        :key="n" 
        class="skeleton-row"
        :style="{ width: getRowWidth(n) }"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  type?: 'card' | 'list' | 'table' | 'article' | 'image' | 'custom' | 'default';
  rows?: number;
  count?: number;
  columns?: number;
  paragraphs?: number;
  animated?: boolean;
  showAvatar?: boolean;
  showSubtitle?: boolean;
  showActions?: boolean;
  showCaption?: boolean;
  imageWidth?: string;
  imageHeight?: string;
  aspectRatio?: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  rows: 3,
  count: 3,
  columns: 4,
  paragraphs: 3,
  animated: true,
  showAvatar: false,
  showSubtitle: false,
  showActions: false,
  showCaption: false,
  imageWidth: '100%',
  imageHeight: '200px',
  aspectRatio: 'auto'
});

// 计算行宽度
const getRowWidth = (rowIndex: number): string => {
  const widths = ['100%', '85%', '60%', '90%', '75%'];
  return widths[(rowIndex - 1) % widths.length];
};

// 计算段落行数
const getParagraphLines = (paragraphIndex: number): number => {
  const lineCounts = [4, 3, 5, 2, 4];
  return lineCounts[(paragraphIndex - 1) % lineCounts.length];
};

// 计算行宽度（文章模式）
const getLineWidth = (lineIndex: number, totalLines: number): string => {
  if (lineIndex === totalLines) {
    // 最后一行通常较短
    return Math.random() > 0.5 ? '60%' : '75%';
  }
  return '100%';
};
</script>

<style scoped lang="less">
.skeleton-loader {
  .skeleton-base {
    background: linear-gradient(90deg, #f2f2f2 25%, #e6e6e6 50%, #f2f2f2 75%);
    background-size: 200% 100%;
    border-radius: 4px;
  }

  &.animated {
    .skeleton-base {
      animation: skeleton-loading 1.5s ease-in-out infinite;
    }
  }

  // 基础骨架元素
  .skeleton-avatar {
    .skeleton-base();
    width: 40px;
    height: 40px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .skeleton-title {
    .skeleton-base();
    height: 16px;
    margin-bottom: 8px;
  }

  .skeleton-subtitle {
    .skeleton-base();
    height: 12px;
    width: 70%;
  }

  .skeleton-row {
    .skeleton-base();
    height: 12px;
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .skeleton-button {
    .skeleton-base();
    height: 32px;
    width: 80px;
    border-radius: 16px;
  }

  // 卡片骨架屏
  .skeleton-card {
    background: white;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;

    .skeleton-header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      .skeleton-content {
        flex: 1;
        margin-left: 12px;
      }
    }

    .skeleton-body {
      margin-bottom: 16px;
    }

    .skeleton-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
    }
  }

  // 列表骨架屏
  .skeleton-list {
    .skeleton-list-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background: white;
      border-bottom: 1px solid #f0f0f0;

      .skeleton-content {
        flex: 1;
        margin-left: 12px;
      }

      .skeleton-action {
        .skeleton-base();
        width: 24px;
        height: 24px;
        border-radius: 4px;
      }
    }
  }

  // 表格骨架屏
  .skeleton-table {
    background: white;
    border-radius: 8px;
    overflow: hidden;

    .skeleton-table-header {
      display: flex;
      background: #f7f8fa;
      padding: 12px 0;

      .skeleton-table-cell {
        .skeleton-base();
        flex: 1;
        height: 14px;
        margin: 0 12px;
      }
    }

    .skeleton-table-row {
      display: flex;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .skeleton-table-cell {
        .skeleton-base();
        flex: 1;
        height: 12px;
        margin: 0 12px;
      }
    }
  }

  // 文章骨架屏
  .skeleton-article {
    background: white;
    border-radius: 8px;
    padding: 20px;

    .skeleton-article-title {
      .skeleton-base();
      height: 24px;
      width: 80%;
      margin-bottom: 16px;
    }

    .skeleton-article-meta {
      display: flex;
      align-items: center;
      margin-bottom: 24px;

      .skeleton-meta-info {
        margin-left: 12px;

        .skeleton-author {
          .skeleton-base();
          height: 14px;
          width: 80px;
          margin-bottom: 4px;
        }

        .skeleton-date {
          .skeleton-base();
          height: 12px;
          width: 60px;
        }
      }
    }

    .skeleton-article-content {
      .skeleton-paragraph {
        margin-bottom: 16px;

        .skeleton-line {
          .skeleton-base();
          height: 14px;
          margin-bottom: 6px;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }

  // 图片骨架屏
  .skeleton-image {
    .skeleton-image-placeholder {
      .skeleton-base();
      display: flex;
      align-items: center;
      justify-content: center;
      color: #c0c4cc;
      font-size: 14px;

      &::before {
        content: '图片加载中...';
      }
    }

    .skeleton-image-caption {
      .skeleton-base();
      height: 12px;
      width: 60%;
      margin-top: 8px;
    }
  }

  // 默认骨架屏
  .skeleton-default {
    padding: 16px;
  }
}

// 动画效果
@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

// 响应式设计
@media (max-width: 375px) {
  .skeleton-loader {
    .skeleton-card {
      padding: 12px;
    }

    .skeleton-article {
      padding: 16px;
    }

    .skeleton-list-item {
      padding: 10px 12px;
    }
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .skeleton-loader {
    .skeleton-base {
      background: linear-gradient(90deg, #2a2a2a 25%, #3a3a3a 50%, #2a2a2a 75%);
    }

    .skeleton-card,
    .skeleton-article,
    .skeleton-table {
      background: #1e1e1e;
    }

    .skeleton-list-item {
      background: #1e1e1e;
      border-bottom-color: #333;
    }

    .skeleton-table-header {
      background: #2a2a2a;
    }

    .skeleton-table-row {
      border-bottom-color: #333;
    }
  }
}
</style>