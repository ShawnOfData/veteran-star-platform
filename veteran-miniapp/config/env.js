/**
 * 环境配置
 *
 * 切换环境：
 *   - 本地开发：将 ENV 改为 'dev'
 *   - 生产发布：将 ENV 改为 'prod'，并填写真实服务器域名（必须 HTTPS）
 *
 * 微信小程序不支持 process.env，所以通过此文件手动切换。
 * 发布前请务必确认 ENV 与 prod.baseUrl 正确。
 */
const ENV = 'dev'

const configs = {
  dev: {
    baseUrl: 'http://localhost:8080/api'
  },
  prod: {
    // 生产环境部署时替换为真实域名（微信小程序要求 HTTPS + 已配置 request 合法域名）
    baseUrl: 'https://your-server-domain.com/api'
  }
}

const current = configs[ENV] || configs.dev

module.exports = {
  ENV,
  baseUrl: current.baseUrl
}
