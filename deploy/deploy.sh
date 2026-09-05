#!/bin/bash
# ========================================
# 戎归·星辉 一键部署脚本
# 在服务器上执行：./deploy.sh
# ========================================
set -e

echo "=========================================="
echo "  戎归·星辉 Docker 部署脚本"
echo "=========================================="

# 1. 检查 .env 文件
if [ ! -f .env ]; then
    echo "[!] 未找到 .env 文件，从模板创建..."
    cp .env.example .env
    echo "[!] 请编辑 .env 修改密码和密钥后重新运行此脚本"
    echo "    vi .env"
    exit 1
fi

# 2. 检查必要文件
echo "[1/5] 检查文件..."
for f in app.jar html/index.html soldier.sql nginx.conf Dockerfile.app docker-compose.yml; do
    if [ ! -f "$f" ]; then
        echo "[X] 缺少文件: $f"
        exit 1
    fi
done
echo "    [OK] 所有文件就绪"

# 3. 检查 Docker 环境
echo "[2/5] 检查 Docker..."
if ! command -v docker &> /dev/null; then
    echo "[X] Docker 未安装，请先安装 Docker"
    exit 1
fi
if ! docker compose version &> /dev/null; then
    echo "[X] Docker Compose 未安装，请先安装 docker-compose-plugin"
    exit 1
fi
echo "    [OK] Docker $(docker --version)"
echo "    [OK] Docker Compose $(docker compose version)"

# 4. 构建后端镜像
echo "[3/5] 构建后端镜像..."
docker compose build app

# 5. 启动服务
echo "[4/5] 启动服务..."
docker compose up -d

# 6. 等待健康检查
echo "[5/5] 等待服务就绪..."
echo "    等待 MySQL 启动..."
sleep 15
echo "    等待后端启动..."
sleep 20

# 检查状态
echo ""
echo "=========================================="
echo "  部署完成！服务状态："
echo "=========================================="
docker compose ps

echo ""
echo "访问地址：http://$(curl -s ifconfig.me 2>/dev/null || echo '服务器IP')"
echo ""
echo "常用命令："
echo "  查看日志：  docker compose logs -f app"
echo "  重启服务：  docker compose restart app"
echo "  停止服务：  docker compose down"
echo "  查看状态：  docker compose ps"
