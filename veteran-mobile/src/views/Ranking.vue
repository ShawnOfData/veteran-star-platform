<!--
  Ranking.vue — 排行榜
  前三名领奖台 + 排名列表
-->
<template>
  <div class="ranking-page">
    <van-nav-bar title="排行榜" left-arrow @click-left="$router.back()" />

    <!-- 前三名领奖台 -->
    <div class="podium" v-if="rankList.length >= 3">
      <div class="podium-item podium-2">
        <div class="podium-avatar">
          <van-icon name="medal" size="28" color="#c0c0c0" />
        </div>
        <p class="podium-name">{{ rankList[1].name }}</p>
        <p class="podium-score">{{ rankList[1].totalPoints }}分</p>
        <div class="podium-bar bar-2">2</div>
      </div>
      <div class="podium-item podium-1">
        <div class="podium-avatar">
          <van-icon name="medal" size="32" color="#ffd700" />
        </div>
        <p class="podium-name">{{ rankList[0].name }}</p>
        <p class="podium-score">{{ rankList[0].totalPoints }}分</p>
        <div class="podium-bar bar-1">1</div>
      </div>
      <div class="podium-item podium-3">
        <div class="podium-avatar">
          <van-icon name="medal" size="28" color="#cd7f32" />
        </div>
        <p class="podium-name">{{ rankList[2].name }}</p>
        <p class="podium-score">{{ rankList[2].totalPoints }}分</p>
        <div class="podium-bar bar-3">3</div>
      </div>
    </div>

    <!-- 第4名及以后 -->
    <van-cell-group inset>
      <van-cell
        v-for="(item, idx) in rankList.slice(3)"
        :key="item.id"
        :class="{ 'my-rank': item.id == store.studentId }"
      >
        <template #title>
          <span class="rank-index">{{ idx + 4 }}</span>
          <span class="rank-name">{{ item.name }}</span>
        </template>
        <template #value>
          <span class="rank-score">{{ item.totalPoints }}分</span>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 前三名不够3人时的列表展示 -->
    <van-cell-group inset v-if="rankList.length < 3 && rankList.length > 0">
      <van-cell
        v-for="(item, idx) in rankList"
        :key="item.id"
        :class="{ 'my-rank': item.id == store.studentId }"
      >
        <template #title>
          <span class="rank-index">{{ idx + 1 }}</span>
          <span class="rank-name">{{ item.name }}</span>
          <span class="rank-college">{{ item.college }}</span>
        </template>
        <template #value>
          <span class="rank-score">{{ item.totalPoints }}分</span>
        </template>
      </van-cell>
    </van-cell-group>

    <LoadingState v-if="loading" height="200px" />
    <EmptyState v-if="!loading && !rankList.length" description="暂无排行数据" />
  </div>
</template>

<script setup>
/**
 * 排行榜页
 * 调用 getRankingList 获取排行榜数据
 * 前三名用领奖台样式展示，其余用列表
 */
import { ref, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getRankingList } from '@/api/studentApp'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const store = useStudentStore()
const rankList = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getRankingList({ page: 1, size: 50 })
    rankList.value = res.data?.records || res.data || []
  } catch (e) { /* 拦截器处理 */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.ranking-page { background: var(--color-bg-page); min-height: 100vh; }

/* 领奖台 */
.podium {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding: 20px 16px;
  background: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  color: #fff;
}
.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 8px;
}
.podium-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.podium-name {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 2px;
}
.podium-score {
  font-size: 11px;
  opacity: 0.8;
  margin-bottom: 8px;
}
.podium-bar {
  width: 60px;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}
.bar-1 { height: 80px; background: linear-gradient(180deg, #ffd700, #e6c200); }
.bar-2 { height: 60px; background: linear-gradient(180deg, #c0c0c0, #a8a8a8); }
.bar-3 { height: 40px; background: linear-gradient(180deg, #cd7f32, #b06b28); }

/* 列表 */
.rank-index {
  display: inline-block;
  width: 28px;
  text-align: center;
  font-weight: 700;
  color: var(--color-text-secondary);
}
.rank-name {
  font-weight: 500;
  margin-left: 4px;
}
.rank-college {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-left: 8px;
}
.rank-score {
  font-weight: 600;
  color: var(--color-primary);
}
.my-rank {
  background: rgba(37, 99, 235, 0.05);
}
.my-rank .rank-name {
  color: var(--color-primary);
}
</style>
