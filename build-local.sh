#!/bin/bash
# Build script for local testing (Option 2)
# Use this if you want to test without creating a GitHub release

set -e

echo "=================================="
echo "Building Whisper Extension Locally"
echo "=================================="

# Build the JAR
echo "Building with Maven..."
mvn clean package -DskipTests

# Check if build succeeded
JAR_FILE="target/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: Build failed - JAR not found at $JAR_FILE"
    exit 1
fi

echo "✅ Build successful!"
echo "📦 JAR location: $JAR_FILE"
echo "📏 Size: $(du -h $JAR_FILE | cut -f1)"

# Copy to Docker build context
echo ""
echo "Copying JAR to Docker build context..."
cp "$JAR_FILE" ./datashare-extension-whisper.jar
echo "✅ Ready for Docker build"

echo ""
echo "=================================="
echo "Next steps:"
echo "=================================="
echo "1. Build Docker image:"
echo "   docker build -f Dockerfile.local -t datashare-whisper:test ."
echo ""
echo "2. Run container:"
echo "   docker run -v \$(pwd)/data:/home/datashare/data datashare-whisper:test"
echo ""
echo "3. Test Whisper pipeline:"
echo "   docker exec <container> java -jar datashare.jar --mode CLI --nlpPipeline WHISPER"
