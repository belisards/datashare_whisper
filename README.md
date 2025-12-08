# Datashare Whisper Transcription Extension

# ⚠️ WARNING ⚠️ 🐞🤖 Buggy AI-Generated Code

Automatically transcribe audio and video files using OpenAI's Whisper model as part of your Datashare document processing pipeline.

## Overview

This extension adds audio/video transcription capabilities to Datashare by integrating OpenAI's [Whisper](https://github.com/openai/whisper) automatic speech recognition (ASR) system. When enabled, it processes audio and video files during the NLP extraction stage, making their spoken content searchable alongside your other documents.

## Features

- **Multi-format Support**: Processes MP3, WAV, MP4, M4A, FLAC, OGG, WebM, AVI, MKV, and many other audio/video formats
- **99 Language Support**: Whisper supports transcription in 99 languages with automatic language detection
- **Flexible Model Selection**: Choose from 5 model sizes (tiny, base, small, medium, large) to balance speed vs. accuracy
- **Searchable Transcriptions**: Transcribed text is indexed in Elasticsearch, making audio content fully searchable
- **Configurable Timeouts**: Set custom timeout values for long recordings
- **Language Override**: Force specific language or use auto-detection

## Prerequisites

### System Requirements

- **Python 3.8+** installed on your system
- **OpenAI Whisper** Python package
- **FFmpeg** (required by Whisper for audio processing)

### Installation

#### 1. Install FFmpeg

**Ubuntu/Debian:**
```bash
sudo apt update && sudo apt install ffmpeg
```

**macOS:**
```bash
brew install ffmpeg
```

**Windows:**
Download from [ffmpeg.org](https://ffmpeg.org/download.html) and add to PATH.

#### 2. Install Whisper

```bash
pip install openai-whisper
```

Or for GPU acceleration (if you have CUDA installed):
```bash
pip install openai-whisper torch --index-url https://download.pytorch.org/whl/cu118
```

#### 3. Verify Installation

```bash
python3 -c "import whisper; print(whisper.__version__)"
```

## Usage

### Basic Usage

Run Datashare with the WHISPER pipeline enabled:

```bash
java -jar datashare.jar \
  --mode CLI \
  --nlpPipeline WHISPER \
  --dataDir /path/to/data
```

### Configuration Options

The Whisper extension supports the following command-line options:

| Option | Description | Default | Values |
|--------|-------------|---------|--------|
| `--whisperModel` | Model size to use | `base` | `tiny`, `base`, `small`, `medium`, `large` |
| `--whisperLanguage` | Force specific language | `auto` | Language code (e.g., `en`, `fr`, `es`) or `auto` |
| `--whisperTimeout` | Transcription timeout (seconds) | `300` | Any positive integer |
| `--whisperPythonPath` | Path to Python executable | `python3` | Full path to Python binary |

### Examples

#### Use Small Model for Faster Processing

```bash
java -jar datashare.jar \
  --mode CLI \
  --nlpPipeline WHISPER \
  --whisperModel tiny \
  --dataDir /path/to/data
```

#### Force French Language

```bash
java -jar datashare.jar \
  --mode CLI \
  --nlpPipeline WHISPER \
  --whisperLanguage fr \
  --dataDir /path/to/data
```

#### Use Large Model for Best Accuracy

```bash
java -jar datashare.jar \
  --mode CLI \
  --nlpPipeline WHISPER \
  --whisperModel large \
  --whisperTimeout 600 \
  --dataDir /path/to/data
```

#### Custom Python Path

```bash
java -jar datashare.jar \
  --mode CLI \
  --nlpPipeline WHISPER \
  --whisperPythonPath /usr/local/bin/python3.11 \
  --dataDir /path/to/data
```

#### Combine with Other Pipelines

```bash
java -jar datashare.jar \
  --mode CLI \
  --nlpPipeline WHISPER,CORENLP \
  --dataDir /path/to/data
```

## Supported Formats

### Audio Formats
- **MP3** (`.mp3`) - MPEG Audio Layer 3
- **WAV** (`.wav`) - Waveform Audio File Format
- **FLAC** (`.flac`) - Free Lossless Audio Codec
- **OGG** (`.ogg`) - Ogg Vorbis
- **M4A** (`.m4a`) - MPEG-4 Audio
- **AAC** (`.aac`) - Advanced Audio Coding
- **WMA** (`.wma`) - Windows Media Audio
- **AIFF** (`.aiff`) - Audio Interchange File Format

### Video Formats
- **MP4** (`.mp4`) - MPEG-4 Video
- **AVI** (`.avi`) - Audio Video Interleave
- **MKV** (`.mkv`) - Matroska Video
- **MOV** (`.mov`) - QuickTime Movie
- **WMV** (`.wmv`) - Windows Media Video
- **FLV** (`.flv`) - Flash Video
- **WebM** (`.webm`) - WebM Video
- **3GP** (`.3gp`) - 3GPP Multimedia

## Model Selection Guide

Choose the appropriate Whisper model based on your needs:

| Model | Size | Speed | Accuracy | Use Case |
|-------|------|-------|----------|----------|
| **tiny** | ~39 MB | Fastest | Basic | Quick previews, testing |
| **base** | ~74 MB | Fast | Good | Default choice, balanced performance |
| **small** | ~244 MB | Moderate | Better | Production use, decent quality |
| **medium** | ~769 MB | Slow | Great | High-quality transcriptions |
| **large** | ~1550 MB | Slowest | Best | Maximum accuracy, research |

**Recommendation**: Start with `base` for testing, then use `small` or `medium` for production.

## Supported Languages

Whisper supports 99 languages. Some examples:

- English (en)
- Spanish (es)
- French (fr)
- German (de)
- Italian (it)
- Portuguese (pt)
- Dutch (nl)
- Russian (ru)
- Chinese (zh)
- Japanese (ja)
- Korean (ko)
- Arabic (ar)
- Polish (pl)
- Ukrainian (uk)
- Swedish (sv)
- Danish (da)
- Norwegian (no)

For the full list, see [Whisper documentation](https://github.com/openai/whisper#available-models-and-languages).

## Performance Considerations

### Processing Time

Transcription speed depends on:
- **Model size**: Larger models = slower but more accurate
- **Audio length**: Longer files take more time
- **Hardware**: CPU vs. GPU makes a significant difference
- **Audio quality**: Clearer audio processes faster

**Typical speeds (CPU, base model)**:
- 1 minute of audio ≈ 10-30 seconds processing
- 1 hour of audio ≈ 10-30 minutes processing

### GPU Acceleration

For much faster transcription, use a CUDA-enabled GPU:

```bash
pip install openai-whisper torch --index-url https://download.pytorch.org/whl/cu118
```

With GPU, the same 1-hour audio might process in 2-5 minutes.

### Memory Usage

- **tiny**: ~1 GB RAM
- **base**: ~1 GB RAM
- **small**: ~2 GB RAM
- **medium**: ~5 GB RAM
- **large**: ~10 GB RAM

## Troubleshooting

### Whisper Not Found

**Error**: `Whisper is not installed or Python is not available`

**Solution**:
```bash
pip install openai-whisper
# Verify
python3 -c "import whisper; print(whisper.__version__)"
```

### FFmpeg Not Found

**Error**: `ffmpeg: command not found` or similar

**Solution**: Install FFmpeg (see [Prerequisites](#prerequisites))

### Timeout Errors

**Error**: `Transcription timed out after 300 seconds`

**Solution**: Increase timeout for longer files:
```bash
--whisperTimeout 600
```

### Python Version Issues

**Error**: Python version incompatibility

**Solution**: Use Python 3.8+:
```bash
--whisperPythonPath /usr/bin/python3.9
```

### Memory Issues

**Error**: Out of memory errors

**Solution**: Use a smaller model:
```bash
--whisperModel tiny
```

## How It Works

1. **Detection**: During NLP processing, Whisper pipeline detects audio/video files by MIME type or file extension
2. **Transcription**: Executes Whisper Python script to transcribe the audio
3. **Parsing**: Extracts transcribed text from Whisper's JSON output
4. **Indexing**: Returns transcription to be indexed in Elasticsearch as document content
5. **Search**: Transcribed text becomes searchable like any other document

## Building from Source

```bash
# Build the extension
cd datashare-extension-whisper
mvn clean package

# The JAR will be created at:
# target/datashare-extension-whisper-{version}-jar-with-dependencies.jar
```

## Testing

```bash
# Run tests
mvn test

# Run specific test
mvn test -Dtest=WhisperPipelineTest
```

## Development

### Project Structure

```
datashare-extension-whisper/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/org/icij/datashare/text/nlp/whisper/
    │       └── WhisperPipeline.java
    └── test/
        └── java/org/icij/datashare/text/nlp/whisper/
            └── WhisperPipelineTest.java
```

### Key Classes

- **WhisperPipeline**: Main pipeline implementation extending `AbstractPipeline`
- **WhisperPipelineTest**: Unit tests for the pipeline

## License

This extension is part of Datashare and is licensed under [GNU Affero General Public License v3.0](../LICENSE.txt).

Whisper is developed by OpenAI and is licensed under the MIT License.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Support

For issues and questions:

- **Datashare Issues**: https://github.com/ICIJ/datashare/issues
- **Whisper Issues**: https://github.com/openai/whisper/issues
- **Documentation**: https://datashare.icij.org

## Changelog

### Version 1.0.0 (Initial Release)

- Audio/video file detection by MIME type and extension
- Whisper integration with configurable models
- Support for 99 languages
- Configurable timeout and Python path
- Automatic language detection
- Unit tests and documentation
