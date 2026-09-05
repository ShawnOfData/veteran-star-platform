<template>
  <div class="panel-card">
    <div class="panel-header">
      <h3>常见问题</h3>
      <span class="panel-more">全部问题 →</span>
    </div>
    <el-collapse v-model="activeNames" class="faq-collapse">
      <el-collapse-item
        v-for="(faq, i) in faqs"
        :key="i"
        :name="String(i)"
      >
        <template #title>
          <span class="faq-q">{{ faq.q }}</span>
        </template>
        <div class="faq-a">{{ faq.a }}</div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeNames = ref([])

const faqs = [
  {
    q: '如何获取积分？',
    a: '通过参与社会服务、申报个人荣誉、提交技能证书等方式获取积分，管理员审核通过后积分自动到账。'
  },
  {
    q: '积分可以用于什么？',
    a: '积分用于个人成长画像、排行榜展示，后续将开放积分兑换、活动报名等服务，敬请期待。'
  },
  {
    q: '积分多久到账？',
    a: '提交的服役经历、荣誉、证书经管理员审核后积分即时发放，一般 1-3 个工作日内完成审核。'
  },
  {
    q: '申报材料被驳回怎么办？',
    a: '可在"我的提交"中查看驳回原因，根据提示补充或修正材料后重新提交即可。'
  }
]
</script>

<style scoped>
.panel-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #edf1f7;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
}
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.panel-header h3 { font-size: 16px; font-weight: 600; color: #0F172A; }
.panel-more { font-size: 12px; color: var(--color-military); cursor: pointer; }

.faq-collapse { border: none; }
.faq-collapse :deep(.el-collapse-item__header) {
  font-size: 13px; color: #303133;
  height: 44px; border-bottom: 1px solid #f0f2f5;
}
.faq-q { font-size: 13px; }
.faq-a {
  font-size: 12px; color: #64748b; line-height: 1.8;
  padding: 0 8px 12px; background: #f8fafc; border-radius: 8px;
}
</style>
