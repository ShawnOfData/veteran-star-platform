import { ref, onMounted, onUnmounted } from 'vue'

export function useParticles(canvasRef, options = {}) {
  const {
    count = 80,
    color = 'rgba(196, 163, 90, 0.6)',
    lineColor = 'rgba(196, 163, 90, 0.12)',
    maxDistance = 120,
    speed = 0.3
  } = options

  let animationId = null
  let particles = []
  let targetX = null
  let targetY = null

  function createParticle(w, h) {
    return {
      x: Math.random() * w,
      y: Math.random() * h,
      vx: (Math.random() - 0.5) * speed,
      vy: (Math.random() - 0.5) * speed,
      radius: Math.random() * 2 + 1,
      baseVx: 0,
      baseVy: 0
    }
  }

  function initParticles(w, h) {
    particles = []
    for (let i = 0; i < count; i++) {
      particles.push(createParticle(w, h))
    }
  }

  function animate() {
    const canvas = canvasRef.value
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    const w = canvas.width
    const h = canvas.height

    ctx.clearRect(0, 0, w, h)

    particles.forEach(p => {
      if (targetX !== null && targetY !== null) {
        const dx = targetX - p.x
        const dy = targetY - p.y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 300) {
          const force = (300 - dist) / 300 * 0.02
          p.vx += dx * force
          p.vy += dy * force
        }
      }

      p.vx += (p.baseVx - p.vx) * 0.02
      p.vy += (p.baseVy - p.vy) * 0.02

      p.x += p.vx
      p.y += p.vy

      if (p.x < 0) p.x = w
      if (p.x > w) p.x = 0
      if (p.y < 0) p.y = h
      if (p.y > h) p.y = 0

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
      ctx.fillStyle = color
      ctx.fill()
    })

    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < maxDistance) {
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.strokeStyle = lineColor
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      }
    }

    animationId = requestAnimationFrame(animate)
  }

  function resize() {
    const canvas = canvasRef.value
    if (!canvas) return
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    initParticles(canvas.width, canvas.height)
  }

  function setTarget(x, y) {
    targetX = x
    targetY = y
  }

  function clearTarget() {
    targetX = null
    targetY = null
  }

  onMounted(() => {
    resize()
    window.addEventListener('resize', resize)
    animate()
  })

  onUnmounted(() => {
    if (animationId) cancelAnimationFrame(animationId)
    window.removeEventListener('resize', resize)
  })

  return { setTarget, clearTarget }
}