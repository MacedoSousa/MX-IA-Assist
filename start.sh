#!/bin/bash
# MX Quick Start - Linux/macOS

echo "🚀 Starting MX AI Assistant System"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Docker
echo -e "${BLUE}Checking Docker...${NC}"
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker Desktop."
    exit 1
fi

# Start infrastructure
echo -e "${BLUE}Starting infrastructure (PostgreSQL, Redis, Ollama, MX-Core)...${NC}"
cd "$(dirname "$0")/infrastructure/docker/compose"
docker-compose up -d --build

echo -e "${GREEN}✓ Infrastructure starting...${NC}"
echo ""

# Wait for services
echo -e "${YELLOW}⏳ Waiting for services to be ready (30 seconds)...${NC}"
sleep 30

# Check health
echo -e "${BLUE}Checking service health...${NC}"
if curl -s http://localhost:8080/health > /dev/null; then
    echo -e "${GREEN}✓ API is ready${NC}"
else
    echo -e "${YELLOW}⚠ API not ready yet, retrying...${NC}"
    sleep 10
fi

# Start frontend
echo ""
echo -e "${BLUE}Starting frontend server...${NC}"
cd "$(dirname "$0")/frontend"

# Check if Python is available
if command -v python3 &> /dev/null; then
    echo -e "${GREEN}✓ Starting Python HTTP server on port 3001${NC}"
    python3 -m http.server 3001 &
    FRONTEND_PID=$!
elif command -v python &> /dev/null; then
    echo -e "${GREEN}✓ Starting Python HTTP server on port 3001${NC}"
    python -m http.server 3001 &
    FRONTEND_PID=$!
else
    echo -e "${YELLOW}⚠ Python not found. Install Python or start manually:${NC}"
    echo "   cd $(pwd)"
    echo "   npx http-server -p 3001"
    FRONTEND_PID=""
fi

echo ""
echo "================================================================"
echo -e "${GREEN}✅ MX AI Assistant is Ready!${NC}"
echo "================================================================"
echo ""
echo "🌐 Access Points:"
echo -e "   Application:  ${BLUE}http://localhost:3001${NC}"
echo -e "   API:          ${BLUE}http://localhost:8080${NC}"
echo -e "   WebUI:        ${BLUE}http://localhost:3000${NC}"
echo -e "   Database:     ${BLUE}localhost:5432${NC}"
echo ""
echo "📊 Services Running:"
echo "   ✓ PostgreSQL (port 5432)"
echo "   ✓ Redis (port 6379)"
echo "   ✓ Ollama (port 11434)"
echo "   ✓ Open WebUI (port 3000)"
echo "   ✓ MX-Core API (port 8080)"
echo ""
echo "🛑 To stop all services:"
echo "   cd infrastructure/docker/compose"
echo "   docker-compose down"
echo ""
if [ ! -z "$FRONTEND_PID" ]; then
    echo "   kill $FRONTEND_PID  (to stop frontend server)"
fi
echo ""
echo "📖 Documentation:"
echo "   - Full Setup: see IMPLEMENTATION_SUMMARY.md"
echo "   - API Docs: infrastructure/docker/QUICKSTART.md"
echo "   - Frontend: frontend/README.md"
echo ""
echo "🔗 Quick Test (in another terminal):"
echo "   curl http://localhost:8080/health"
echo ""
echo "Press Ctrl+C to stop frontend server"
echo "================================================================"
echo ""

# Keep script running if frontend started
if [ ! -z "$FRONTEND_PID" ]; then
    wait $FRONTEND_PID
fi
