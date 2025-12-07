#!/bin/bash
# Verification script for Whisper extension installation

set -e

CONTAINER_NAME=${1:-datashare-whisper-test}

echo "🔍 Verifying Whisper Extension Installation"
echo "Container: $CONTAINER_NAME"
echo "=========================================="

# Check if container is running
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo "❌ Container '$CONTAINER_NAME' is not running"
    echo ""
    echo "Start it with:"
    echo "  docker-compose -f docker-compose.test.yml up -d"
    exit 1
fi

echo ""
echo "1️⃣  Checking extensions directory..."
docker exec "$CONTAINER_NAME" ls -lah /home/datashare/extensions/ || {
    echo "❌ Extensions directory not found"
    exit 1
}

echo ""
echo "2️⃣  Checking Whisper JAR..."
if docker exec "$CONTAINER_NAME" test -f /home/datashare/extensions/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar; then
    echo "✅ Whisper JAR found"
    docker exec "$CONTAINER_NAME" ls -lh /home/datashare/extensions/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar
else
    echo "❌ Whisper JAR not found"
    exit 1
fi

echo ""
echo "3️⃣  Checking Python Whisper package..."
docker exec "$CONTAINER_NAME" python3 -c "import whisper; print('✅ Whisper version:', whisper.__version__)" || {
    echo "❌ Whisper Python package not installed"
    exit 1
}

echo ""
echo "4️⃣  Checking FFmpeg..."
docker exec "$CONTAINER_NAME" ffmpeg -version | head -1 || {
    echo "❌ FFmpeg not installed"
    exit 1
}

echo ""
echo "5️⃣  Checking Datashare can see the extension..."
docker exec "$CONTAINER_NAME" sh -c "cd /home/datashare && ls extensions/*.jar" || {
    echo "❌ Datashare cannot see extensions"
    exit 1
}

echo ""
echo "=========================================="
echo "✅ All checks passed!"
echo ""
echo "📝 Next steps:"
echo "  1. Test with an audio file:"
echo "     docker exec -it $CONTAINER_NAME bash"
echo "     java -jar datashare.jar --mode CLI --nlpPipeline WHISPER --dataDir /home/datashare/data"
echo ""
echo "  2. Check logs:"
echo "     docker logs $CONTAINER_NAME"
echo ""
echo "  3. Access Datashare UI:"
echo "     http://localhost:8080"
