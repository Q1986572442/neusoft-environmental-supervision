#!/bin/bash
# =====================================================
# 东软环保公众监督系统 (NEP) - 一键启动脚本
# 启动顺序: 基础设施 → 微服务 → 前端
# =====================================================

set -e

echo "============================================"
echo "  东软环保公众监督系统 (NEP) 启动中..."
echo "============================================"

# ---------- Step 1: 启动基础设施 ----------
echo ""
echo "[1/4] 启动基础设施 (MySQL + Nacos + Redis + MinIO + Sentinel)..."
docker-compose up -d
echo "  等待服务就绪..."
sleep 10

# ---------- Step 2: 启动微服务 ----------
echo ""
echo "[2/4] 编译并启动微服务..."
mvn clean package -DskipTests -q

echo "  启动 nep-service-user (:8082)..."
nohup java -jar nep-service-user/target/nep-service-user-1.0.0.jar > logs/user.log 2>&1 &
echo "  启动 nep-service-feedback (:8083)..."
nohup java -jar nep-service-feedback/target/nep-service-feedback-1.0.0.jar > logs/feedback.log 2>&1 &
echo "  启动 nep-service-content (:8084)..."
nohup java -jar nep-service-content/target/nep-service-content-1.0.0.jar > logs/content.log 2>&1 &
sleep 8

# ---------- Step 3: 启动网关 ----------
echo ""
echo "[3/4] 启动 API Gateway (:9000)..."
nohup java -jar nep-gateway/target/nep-gateway-1.0.0.jar > logs/gateway.log 2>&1 &
sleep 5

# ---------- Step 4: 启动前端 ----------
echo ""
echo "[4/4] 启动前端 (Vite)..."
cd nep-ui && npm install -s && npm run dev &
cd ..

echo ""
echo "============================================"
echo "  系统启动完成!"
echo "============================================"
echo ""
echo "  前端页面:     http://localhost:3000"
echo "  API 网关:     http://localhost:9000"
echo "  Nacos 控制台: http://localhost:8848/nacos (nacos/nacos)"
echo "  Sentinel:     http://localhost:8858 (sentinel/sentinel)"
echo "  MinIO 控制台: http://localhost:9001 (minioadmin/minioadmin)"
echo "  Knife4j 文档: http://localhost:9000/doc.html"
echo ""
echo "  微服务列表:"
echo "    - 用户服务: http://localhost:8082"
echo "    - 反馈服务: http://localhost:8083"
echo "    - 内容服务: http://localhost:8084"
echo ""
