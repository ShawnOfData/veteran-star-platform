/**
 * PostCSS 配置
 * pxtorem: 将 px 自动转换为 rem，设计稿宽度 375px
 */
export default {
  plugins: {
    'postcss-pxtorem': {
      // 根字体大小（375px 设计稿对应 37.5px）
      rootValue: 37.5,
      // 允许所有属性转换
      propList: ['*'],
      // 忽略 Vant 的 1px 边框
      selectorBlackList: ['van-'],
      // 最小转换值，1px 不转换
      minPixelValue: 2
    }
  }
}
