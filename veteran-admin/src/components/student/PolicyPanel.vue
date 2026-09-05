<template>
  <div class="panel-card">
    <div class="panel-header">
      <h3>政策速览</h3>
      <span class="badge-building">功能建设中 · 敬请期待</span>
    </div>
    <div class="policy-list">
      <div class="policy-item">
        <div class="policy-icon" style="background:#eff6ff;color:#2563EB">
          <svg class="icon-svg" viewBox="0 0 24 24"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
        </div>
        <div class="policy-info">
          <div class="policy-name">就业创业政策</div>
          <div class="policy-desc">退役大学生士兵就业创业扶持与税收优惠</div>
        </div>
      </div>
      <div class="policy-item">
        <div class="policy-icon" style="background:#ecfdf5;color:#10b981">
          <svg class="icon-svg" viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c0 1.1 2.7 2 6 2s6-.9 6-2v-5"/></svg>
        </div>
        <div class="policy-info">
          <div class="policy-name">教育培训政策</div>
          <div class="policy-desc">退役士兵职业技能培训与学历提升</div>
        </div>
      </div>
      <div class="policy-item">
        <div class="policy-icon" style="background:#fff7ed;color:#f97316">
          <svg class="icon-svg" viewBox="0 0 24 24"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
        </div>
        <div class="policy-info">
          <div class="policy-name">医疗保障政策</div>
          <div class="policy-desc">退役大学生士兵医疗优先与健康管理服务</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #edf1f7;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
}
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.panel-header h3 { font-size: 16px; font-weight: 600; color: #0F172A; }
.badge-building {
  font-size: 11px; color: #94a3b8; background: #f1f5f9;
  padding: 3px 12px; border-radius: 6px; font-weight: 500;
}

.policy-list { display: flex; flex-direction: column; gap: 10px; }
.policy-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: 8px;
  background: #f8fafc;
}
.policy-icon {
  width: 36px; height: 36px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.policy-info { flex: 1; min-width: 0; }
.policy-name { font-size: 13px; font-weight: 600; color: #0F172A; }
.policy-desc { font-size: 11px; color: #94a3b8; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.icon-svg {
  width: 18px; height: 18px; fill: none; stroke: currentColor;
  stroke-width: 2; stroke-linecap: round; stroke-linejoin: round;
}
</style>
