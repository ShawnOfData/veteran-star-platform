<template>
  <div class="qmenu fade-in">
    <div class="qmenu-header">
      <h3 class="qmenu-title">快捷服务</h3>
      <div class="qmenu-more" @click="moreVisible = !moreVisible">
        更多服务
        <svg class="icon-svg more-arrow" :class="{ rotate: moreVisible }" viewBox="0 0 24 24" style="width:14px;height:14px"><polyline points="6 9 12 15 18 9"/></svg>
      </div>
    </div>

    <div class="qmenu-grid">
      <div
        v-for="item in primaryItems"
        :key="item.path"
        class="qm-card"
        @click="$router.push(item.path)"
      >
        <div class="qm-icon" :style="{ background: item.bg, color: item.color }">
          <svg class="icon-svg" viewBox="0 0 24 24"><path :d="item.icon"/></svg>
        </div>
        <div class="qm-body">
          <div class="qm-title">{{ item.title }}</div>
          <div class="qm-desc">{{ item.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 更多服务：原位向下展开 -->
    <el-collapse-transition>
      <div v-show="moreVisible" class="qmenu-more-grid">
        <div
          v-for="item in moreItems"
          :key="item.path"
          class="qm-card"
          @click="$router.push(item.path)"
        >
          <div class="qm-icon" :style="{ background: item.bg, color: item.color }">
            <svg class="icon-svg" viewBox="0 0 24 24"><path :d="item.icon"/></svg>
          </div>
          <div class="qm-body">
            <div class="qm-title">{{ item.title }}</div>
            <div class="qm-desc">{{ item.desc }}</div>
          </div>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElCollapseTransition } from 'element-plus'

const moreVisible = ref(false)

const primaryItems = [
  {
    path: '/opportunities',
    title: '机会广场',
    desc: '发现更多就业机会',
    bg: '#eff6ff', color: '#2563EB',
    icon: 'M22 12h-4l-3 9L9 3l-3 9H2'
  },
  {
    path: '/my-applications',
    title: '我的报名',
    desc: '查看报名进展',
    bg: '#fef2f2', color: '#e74c3c',
    icon: 'M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16'
  },
  {
    path: '/resume-create',
    title: '简历制作',
    desc: '在线制作专属简历',
    bg: '#ecfeff', color: '#06b6d4',
    icon: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6'
  },
  {
    path: '/portrait',
    title: '个人画像',
    desc: '我的成长画像',
    bg: '#ecfdf5', color: '#10b981',
    icon: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z'
  }
]

const moreItems = [
  {
    path: '/points',
    title: '积分明细',
    desc: '积分变动记录',
    bg: '#fff7ed', color: '#f97316',
    icon: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8M12 18V6'
  },
  {
    path: '/ranking',
    title: '排行榜',
    desc: '看看谁在发光',
    bg: '#f5f3ff', color: '#8b5cf6',
    icon: 'M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9zM18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9zM4 22h16M10 22V2h4v20'
  },
  {
    path: '/my-favorites',
    title: '我的收藏',
    desc: '收藏的机会',
    bg: '#fffbeb', color: '#f59e0b',
    icon: 'M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 21 12 17.77 5.82 21 7 14.14l-5-4.87 6.91-1.01L12 2z'
  }
]
</script>

<style scoped>
.qmenu {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #edf1f7;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
  margin-bottom: 24px;
}
.qmenu-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.qmenu-title { font-size: 16px; font-weight: 600; color: #0F172A; }
.qmenu-more {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: var(--color-military); cursor: pointer;
  user-select: none;
}
.qmenu-more:hover { color: #1d4ed8; }
.qmenu-more .more-arrow { transition: transform 0.3s ease; }
.qmenu-more .more-arrow.rotate { transform: rotate(180deg); }

.qmenu-grid,
.qmenu-more-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.qmenu-more-grid { margin-top: 14px; }

.qm-card {
  display: flex; align-items: center; gap: 12px;
  padding: 16px; border-radius: 8px;
  background: #f8fafc; border: 1px solid #f0f3f8;
  cursor: pointer; transition: all 0.25s;
}
.qm-card:hover {
  transform: translateY(-3px);
  background: #fff;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.08);
  border-color: #dbeafe;
}
.qm-icon {
  width: 40px; height: 40px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.qm-body { flex: 1; min-width: 0; }
.qm-title { font-size: 14px; font-weight: 600; color: #0F172A; }
.qm-desc { font-size: 12px; color: #94a3b8; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.icon-svg {
  width: 20px; height: 20px; fill: none; stroke: currentColor;
  stroke-width: 2; stroke-linecap: round; stroke-linejoin: round;
}
</style>
