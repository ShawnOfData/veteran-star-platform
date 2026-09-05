<template>
  <div class="wbanner fade-in">
    <div class="wbanner-bg"></div>
    <div class="wbanner-content">
      <div class="wbanner-left">
        <div class="wbanner-greeting">你好，<span>{{ studentName }}</span><img class="wbanner-clap" src="/鼓掌.png" alt="鼓掌" /></div>
        <div class="wbanner-sub">欢迎回来退役大学生士兵服务管理系统</div>
        <div class="wbanner-quote">仰山慕水，达观博物</div>
        <div class="wbanner-meta">
          <div class="wbanner-tag">
            <span class="dot"></span>
            {{ currentDate }}
          </div>
        </div>
      </div>
      <div class="wbanner-right">
        <div class="wbanner-score">
          <div class="wbanner-score-value">{{ totalPoints }}</div>
          <div class="wbanner-score-label">我的积分</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  studentName: { type: String, default: '' },
  totalPoints: { type: Number, default: 0 },
  rank: { type: [Number, String], default: '--' },
  currentDate: { type: String, default: '' },
  levelInfo: {
    type: Object,
    default: () => ({ level: 1, levelName: '青春新兵', distanceToNext: 100 })
  }
})
</script>

<style scoped>
.wbanner {
  position: relative;
  height: 240px;
  border-radius: 0;
  overflow: hidden;
  margin: 0 -24px 24px;
  box-shadow: none;
}
.wbanner-bg {
  position: absolute; inset: 0;
  background: url('/banner-bg.png') center/cover no-repeat;
  z-index: 0;
}
.wbanner-content {
  position: relative; z-index: 2; height: 100%;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 40px;
}
.wbanner-greeting { font-size: 26px; font-weight: 700; color: #0F172A; margin-bottom: 6px; display: flex; align-items: center; gap: 10px; }
.wbanner-clap { width: 30px; height: 30px; object-fit: contain; vertical-align: middle; animation: wbanner-clap-wave 1.2s ease-in-out infinite; }
@keyframes wbanner-clap-wave {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(12deg); }
}
.wbanner-greeting span { color: #1F2937; }
.wbanner-sub { font-size: 14px; color: #475569; margin-bottom: 8px; }
.wbanner-quote { font-size: 14px; color: #1F2937; font-weight: 500; letter-spacing: 2px; }
.wbanner-meta { display: flex; gap: 12px; margin-top: 16px; }
.wbanner-tag {
  display: flex; align-items: center; gap: 6px; font-size: 13px; color: #475569;
  background: none; padding: 0; border: none; backdrop-filter: none;
}
.wbanner-tag .dot { width: 6px; height: 6px; border-radius: 50%; background: #c4a35a; }

.wbanner-right { flex-shrink: 0; text-align: right; }
.wbanner-score-value {
  font-size: 44px; font-weight: 800; color: #0F172A; line-height: 1.1;
  font-family: 'DIN Alternate', 'Segoe UI', sans-serif;
}
.wbanner-score-label { font-size: 13px; color: #64748b; margin-top: 2px; }
</style>
