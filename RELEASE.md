# Creating a GitHub Release

Since the tag couldn't be pushed automatically, here's how to create the release manually:

## Option A: Via GitHub Web UI (Easiest)

1. **Go to your repository:**
   ```
   https://github.com/belisards/datashare
   ```

2. **Navigate to Releases:**
   - Click "Releases" on the right sidebar
   - Click "Create a new release"

3. **Create the release:**
   - **Tag:** `v1.0.0`
   - **Target:** `claude/whisper-extension-restructure-01C4vfmh3ZafbJ5zvLZYmTNe`
   - **Title:** `Release v1.0.0`
   - **Description:**
     ```markdown
     ## Datashare Whisper Transcription Extension v1.0.0

     Audio/Video transcription using OpenAI's Whisper model.

     ### Features
     - Multi-format support (MP3, WAV, MP4, M4A, FLAC, etc.)
     - 99 language support with auto-detection
     - 5 model sizes (tiny to large)
     - Configurable timeouts and options

     ### Installation

     Download the JAR and place in your Datashare extensions directory:
     ```
     wget https://github.com/belisards/datashare/releases/download/v1.0.0/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar
     ```

     See README.md for full documentation.
     ```

4. **Build and upload JAR:**

   **Option 1 - Let GitHub Actions build it:**
   - Check "Set as pre-release" if testing
   - Click "Publish release"
   - GitHub Actions should automatically build and attach the JAR
   - Wait 2-3 minutes for the workflow to complete

   **Option 2 - Upload manually:**
   - Build locally first:
     ```bash
     git checkout claude/whisper-extension-restructure-01C4vfmh3ZafbJ5zvLZYmTNe
     mvn clean package -DskipTests
     ```
   - Drag and drop the JAR file from `target/` into the release assets
   - File: `datashare-extension-whisper-1.0.0-jar-with-dependencies.jar`
   - Click "Publish release"

---

## Option B: Via Command Line (After fixing push permissions)

```bash
# Make sure you're on the right branch
git checkout claude/whisper-extension-restructure-01C4vfmh3ZafbJ5zvLZYmTNe

# Tag the release (already created locally)
git tag v1.0.0  # Already done

# Push the tag (requires fixing git permissions)
git push origin v1.0.0

# GitHub Actions will automatically:
# 1. Build the JAR
# 2. Create the release
# 3. Upload the JAR as an asset
```

---

## Verify Release

After creating the release, verify the URL works:

```bash
wget --spider https://github.com/belisards/datashare/releases/download/v1.0.0/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar

# If successful, you'll see:
# HTTP/1.1 200 OK
```

---

## Then Update Dockerfile

Once the release is created, you can use it in your Dockerfile:

```dockerfile
RUN cd /home/datashare/extensions && \
    wget -q https://github.com/belisards/datashare/releases/download/v1.0.0/datashare-extension-whisper-1.0.0-jar-with-dependencies.jar
```

---

## Troubleshooting

### If GitHub Actions doesn't run:

1. Check workflow file exists: `.github/workflows/release.yml`
2. Check Actions tab on GitHub
3. Ensure Actions are enabled in repository settings

### If you need to rebuild:

1. Delete the tag: `git tag -d v1.0.0`
2. Delete the release on GitHub
3. Make your changes
4. Create tag again: `git tag v1.0.0`
5. Push: `git push origin v1.0.0`
