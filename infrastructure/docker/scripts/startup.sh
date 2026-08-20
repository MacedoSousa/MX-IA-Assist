#!/bin/bash

# MX Infrastructure Startup Script
# This script starts all services for the MX system

set -e

echo "=== Starting MX Infrastructure ==="
echo ""

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_DIR="$PROJECT_DIR/infrastructure/docker/compose"
ENV_FILE="$PROJECT_DIR/infrastructure/docker/env/.env"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env file not found at $ENV_FILE"
    exit 1
fi

echo "📁 Working directory: $COMPOSE_DIR"
echo "🔧 Environment file: $ENV_FILE"
echo ""

# Build mx-core image
echo "🏗️  Building mx-core Docker image..."
cd "$COMPOSE_DIR"
docker-compose build mx-core

echo ""
echo "🚀 Starting services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo ""
echo "✅ Infrastructure Status:"
echo ""

if docker ps | grep -q "mx-postgres"; then
    echo "  ✓ PostgreSQL running on localhost:5432"
else
    echo "  ✗ PostgreSQL not running"
fi

if docker ps | grep -q "mx-redis"; then
    echo "  ✓ Redis running on localhost:6379"
else
    echo "  ✗ Redis not running"
fi

if docker ps | grep -q "mx-ollama"; then
    echo "  ✓ Ollama running on localhost:11434"
else
    echo "  ✗ Ollama not running"
fi

if docker ps | grep -q "mx-open-webui"; then
    echo "  ✓ Open WebUI running on localhost:3000"
else
    echo "  ✗ Open WebUI not running"
fi

if docker ps | grep -q "mx-core"; then
    echo "  ✓ MX-Core running on localhost:8080"
else
    echo "  ✗ MX-Core not running"
fi

echo ""
echo "🌐 Access points:"
echo "  • API:       http://localhost:8080/api"
echo "  • Health:    http://localhost:8080/health"
echo "  • WebUI:     http://localhost:3000"
echo "  • Ollama:    http://localhost:11434"
echo ""
echo "📊 View logs:"
echo "  docker-compose -f $COMPOSE_DIR/docker-compose.yml logs -f"
echo ""
echo "🛑 Stop services:"
echo "  docker-compose -f $COMPOSE_DIR/docker-compose.yml down"
echo ""
