# Whisper Extension Installation Guide

## Quick Start - Option 3: GitHub Release (Recommended)

### Step 1: Create the Release

Follow instructions in [RELEASE.md](RELEASE.md) to create the GitHub release.

### Step 2: Use in Your Dockerfile

```dockerfile
FROM icij/datashare:13.7.1

USER root

# Install Whisper dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install --no-cache-dir openai-whisper

# Install Datashare extensions
RUN mkdir -p /home/datashare/extensions /home/datashare/plugins && \
    cd /home/datashare/extensions && \
    # Whisper extension
    wget -q https://github.com/belisards/datashare/releases/download/v1.0.0/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar && \
    # Neo4j extension (optional)
    wget -q https://github.com/ICIJ/datashare-extension-neo4j/releases/download/1.1.1/datashare-extension-neo4j-1.1.1-jar-with-dependencies.jar && \
    cd /home/datashare/plugins && \
    wget -q https://github.com/ICIJ/datashare-extension-neo4j/releases/download/1.1.1/datashare-plugin-neo4j-graph-widget-1.1.1.tgz && \
    tar -xzf datashare-plugin-neo4j-graph-widget-1.1.1.tgz && \
    rm datashare-plugin-neo4j-graph-widget-1.1.1.tgz && \
    chown -R datashare:datashare /home/datashare/extensions /home/datashare/plugins

USER datashare
```

---

## Alternative - Local Testing

For quick local testing without creating a release:

```bash
# 1. Build the extension
./build-local.sh

# 2. Build Docker image
docker build -f Dockerfile.local -t datashare-whisper:local .

# 3. Run with docker-compose
docker-compose -f docker-compose.test.yml up -d

# 4. Verify installation
./verify-installation.sh

# 5. Test with sample data
docker exec -it datashare-whisper-test bash
# Inside container:
cd /home/datashare/data
# Add some audio/video files
java -jar /home/datashare/datashare.jar --mode CLI --nlpPipeline WHISPER
```

---

## Usage Examples

### CLI Mode

```bash
docker run -v $(pwd)/data:/home/datashare/data \
  datashare-whisper:local \
  --mode CLI \
  --nlpPipeline WHISPER \
  --whisperModel base \
  --whisperLanguage auto \
  --dataDir /home/datashare/data
```

### Server Mode

```bash
docker run -p 8080:8080 \
  -v $(pwd)/data:/home/datashare/data \
  datashare-whisper:local \
  --mode LOCAL \
  --nlpPipeline WHISPER
```

Then access: http://localhost:8080

---

## Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `--whisperModel` | `base` | Model size: tiny, base, small, medium, large |
| `--whisperLanguage` | `auto` | Language code (en, fr, es, etc.) or auto |
| `--whisperTimeout` | `300` | Transcription timeout in seconds |
| `--whisperPythonPath` | `python3` | Path to Python executable |

---

## Verification

### Check Extension Loaded

```bash
# List installed extensions
docker exec <container> ls -la /home/datashare/extensions/

# Should show:
# datashare-extension-whisper-1.0.0-jar-with-dependencies.jar
```

### Check Whisper Available

```bash
# Test Whisper import
docker exec <container> python3 -c "import whisper; print(whisper.__version__)"

# Test FFmpeg
docker exec <container> ffmpeg -version
```

### Run Verification Script

```bash
./verify-installation.sh [container-name]
```

---

## Troubleshooting

### Extension Not Loaded

```bash
# Check logs
docker logs <container>

# Verify JAR exists
docker exec <container> ls -la /home/datashare/extensions/

# Check Java can read it
docker exec <container> java -jar /home/datashare/extensions/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar
```

### Whisper Not Working

```bash
# Check Python installation
docker exec <container> python3 --version

# Check Whisper package
docker exec <container> pip3 list | grep whisper

# Reinstall if needed
docker exec <container> pip3 install --force-reinstall openai-whisper
```

### FFmpeg Missing

```bash
# Install FFmpeg
docker exec -u root <container> apt-get update
docker exec -u root <container> apt-get install -y ffmpeg
```

---

## Performance Tips

1. **Choose the right model:**
   - `tiny` - Fastest, basic accuracy
   - `base` - Good balance (default)
   - `small` - Better quality
   - `medium` - High quality
   - `large` - Best accuracy, slowest

2. **Use GPU acceleration:**
   ```bash
   pip3 install openai-whisper torch --index-url https://download.pytorch.org/whl/cu118
   ```

3. **Increase timeout for long files:**
   ```bash
   --whisperTimeout 600
   ```

---

## Next Steps

- See [README.md](README.md) for full Whisper extension documentation
- See [RELEASE.md](RELEASE.md) for creating GitHub releases
- Report issues: https://github.com/belisards/datashare/issues
