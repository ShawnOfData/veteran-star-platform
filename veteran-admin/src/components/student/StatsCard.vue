<template>
  <div class="stats-grid fade-in">
    <!-- 我的积分 -->
    <div class="s-card">
      <div class="s-head">
        <div class="s-icon" style="background:rgba(245,158,11,0.12);color:#F59E0B">
          <svg class="icon-svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/><path d="M12 18V6"/></svg>
        </div>
        <span class="s-label">我的积分</span>
      </div>
      <div class="s-number gold-text">{{ totalPoints }}</div>
      <div class="s-sub-main">当前累计积分</div>
    </div>

    <!-- 服务次数 -->
    <div class="s-card">
      <div class="s-head">
        <div class="s-icon" style="background:rgba(16,185,129,0.12);color:#10B981">
          <svg class="icon-svg" viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c0 1.1 2.7 2 6 2s6-.9 6-2v-5"/></svg>
        </div>
        <span class="s-label">服务次数</span>
      </div>
      <div class="s-number">{{ serviceCount }}<span class="s-unit">次</span></div>
      <div class="s-sub-main">累计服务时长</div>
      <div class="s-sub">
        {{ totalHours }} <b>小时</b>
      </div>
    </div>

    <!-- 荣誉成就 -->
    <div class="s-card">
      <div class="s-head">
        <div class="s-icon" style="background:rgba(139,92,246,0.12);color:#8B5CF6">
          <svg class="icon-svg" viewBox="0 0 24 24"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9z"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9z"/><path d="M4 22h16"/><path d="M10 22V2h4v20"/></svg>
        </div>
        <span class="s-label">荣誉成就</span>
      </div>
      <div class="s-number">{{ honorCount }}<span class="s-unit">项</span></div>
      <div class="s-sub-main">技能证书</div>
      <div class="s-sub">
        {{ certCount }} <b>项</b>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  totalPoints: { type: Number, default: 0 },
  serviceCount: { type: Number, default: 0 },
  totalHours: { type: Number, default: 0 },
  honorCount: { type: Number, default: 0 },
  certCount: { type: Number, default: 0 },
  levelInfo: {
    type: Object,
    default: () => ({ level: 1, levelName: '青春新兵', progress: 0, distanceToNext: 100 })
  }
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.s-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #edf1f7;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
  transition: all 0.25s;
}
.s-card:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08); }
.s-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.s-icon {
  width: 34px; height: 34px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
}
.s-label { font-size: 13px; color: #64748b; font-weight: 500; }
.s-number { font-size: 32px; font-weight: 800; color: #0F172A; line-height: 1.15; }
.s-number.gold-text {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.s-unit { font-size: 16px; font-weight: 600; color: #94a3b8; margin-left: 2px; }
.s-sub { font-size: 12px; color: #94a3b8; }
.s-sub b { color: #F59E0B; font-weight: 700; }
.s-sub-main { font-size: 12px; color: #94a3b8; margin-top: 10px; }
.s-sub b.green { color: #10B981; }
.s-sub b.purple { color: #8B5CF6; }

.icon-svg {
  width: 20px; height: 20px; fill: none; stroke: currentColor;
  stroke-width: 2; stroke-linecap: round; stroke-linejoin: round;
}
</style>
