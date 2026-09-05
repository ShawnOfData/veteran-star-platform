/**
 * 积分等级前端计算工具
 * 基于 totalPoints 计算等级、等级名称、进度，避免修改后端接口。
 * 每 100 分升一级。
 */

const LEVEL_POINTS = 100 // 每级所需积分

// 等级名称（可随需求扩展）
const LEVEL_NAMES = [
  '青春新兵',
  '军旅标兵',
  '荣誉卫士',
  '卓越先锋',
  '钢铁脊梁',
  '戎归之星'
]

function clamp(n, min, max) {
  return Math.min(Math.max(n, min), max)
}

/**
 * 根据总积分计算等级信息
 * @param {number} totalPoints 总积分
 * @returns {{ level: number, levelName: string, currentLevelPoints: number, nextLevelPoints: number, progress: number, distanceToNext: number }}
 */
export function calcLevel(totalPoints) {
  const points = Math.max(0, Number(totalPoints) || 0)
  const levelIndex = Math.floor(points / LEVEL_POINTS)
  const level = levelIndex + 1
  const name = LEVEL_NAMES[clamp(levelIndex, 0, LEVEL_NAMES.length - 1)]

  const currentLevelPoints = levelIndex * LEVEL_POINTS
  const nextLevelPoints = currentLevelPoints + LEVEL_POINTS
  const progress = Math.round(((points - currentLevelPoints) / LEVEL_POINTS) * 100)
  const distanceToNext = Math.max(0, nextLevelPoints - points)

  return {
    level,
    levelName: name,
    currentLevelPoints,
    nextLevelPoints,
    progress: clamp(progress, 0, 100),
    distanceToNext
  }
}
