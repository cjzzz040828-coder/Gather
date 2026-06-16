#!/bin/bash
# Campus 系统 - Ubuntu 22.04 环境安装脚本（幂等）
set -e

# 通过环境变量传入密码，未设置时使用占位默认值（请在部署前覆盖）
MYSQL_PWD="${MYSQL_PWD:-ChangeMe@MySQL}"
REDIS_PWD="${REDIS_PWD:-ChangeMe@Redis}"
DB_NAME='campus-system'

echo "=========================================="
echo "  [1/9] 更新 apt 索引"
echo "=========================================="
export DEBIAN_FRONTEND=noninteractive
sudo apt-get update -y

echo "=========================================="
echo "  [2/9] 安装 Java 17"
echo "=========================================="
if ! command -v java >/dev/null 2>&1; then
  sudo apt-get install -y openjdk-17-jdk-headless
fi
java -version

echo "=========================================="
echo "  [3/9] 安装 MySQL"
echo "=========================================="
if ! command -v mysql >/dev/null 2>&1; then
  sudo apt-get install -y mysql-server
fi
sudo systemctl enable --now mysql
# 设置 root 密码 + 本地 socket 鉴权改为密码鉴权（幂等）
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PWD}'; FLUSH PRIVILEGES;" 2>/dev/null \
  || mysql -uroot -p"${MYSQL_PWD}" -e "SELECT 1;" >/dev/null 2>&1 \
  || { echo "MySQL root 密码设置失败"; exit 1; }
mysql -uroot -p"${MYSQL_PWD}" -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "MySQL OK，库 ${DB_NAME} 已就绪"

echo "=========================================="
echo "  [4/9] 安装 Redis"
echo "=========================================="
if ! command -v redis-server >/dev/null 2>&1; then
  sudo apt-get install -y redis-server
fi
# 设置密码（幂等：先删旧行再加）
sudo sed -i '/^requirepass /d' /etc/redis/redis.conf
echo "requirepass ${REDIS_PWD}" | sudo tee -a /etc/redis/redis.conf >/dev/null
sudo systemctl enable --now redis-server
sudo systemctl restart redis-server
echo "Redis OK"

echo "=========================================="
echo "  [5/9] 安装 Nginx"
echo "=========================================="
if ! command -v nginx >/dev/null 2>&1; then
  sudo apt-get install -y nginx
fi
sudo systemctl enable --now nginx

echo "=========================================="
echo "  [6/9] 创建部署目录"
echo "=========================================="
sudo mkdir -p /opt/campus-system/backend /opt/campus-system/frontend/web /opt/campus-system/sql /opt/campus-system/logs
sudo chown -R ubuntu:ubuntu /opt/campus-system

echo "=========================================="
echo "  [7/9] 配置 Nginx"
echo "=========================================="
sudo tee /etc/nginx/conf.d/campus.conf >/dev/null << 'EOF'
server {
    listen 80;
    server_name _;
    client_max_body_size 100M;

    location / {
        root /opt/campus-system/frontend/web;
        try_files $uri $uri/ /index.html;
        index index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 300s;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    location /druid {
        proxy_pass http://127.0.0.1:8080/druid;
        proxy_set_header Host $host;
    }

    location /admin {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF
# 关掉默认站点避免冲突
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
echo "Nginx OK"

echo "=========================================="
echo "  [8/9] 配置 systemd 服务"
echo "=========================================="
sudo tee /etc/systemd/system/campus-admin.service >/dev/null << 'EOF'
[Unit]
Description=Campus Admin System
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/campus-system/backend
ExecStart=/usr/bin/java -Xms256m -Xmx640m -jar /opt/campus-system/backend/campus-starter-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/campus-system/logs/app.log
StandardError=append:/opt/campus-system/logs/error.log

[Install]
WantedBy=multi-user.target
EOF
sudo systemctl daemon-reload

echo "=========================================="
echo "  [9/9] 配置 swap（内存仅 1.9G）"
echo "=========================================="
if ! sudo swapon --show | grep -q '/swapfile'; then
  sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab >/dev/null
fi
free -h

echo ""
echo "=========================================="
echo "  环境安装完成！"
echo "=========================================="
