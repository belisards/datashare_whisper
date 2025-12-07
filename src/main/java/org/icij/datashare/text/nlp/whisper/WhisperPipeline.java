package org.icij.datashare.text.nlp.whisper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.icij.datashare.PropertiesProvider;
import org.icij.datashare.text.Document;
import org.icij.datashare.text.Language;
import org.icij.datashare.text.NamedEntity;
import org.icij.datashare.text.nlp.AbstractPipeline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Whisper Transcription Pipeline
 *
 * This pipeline transcribes audio and video files using OpenAI's Whisper model.
 * It supports 99 languages and various audio/video formats.
 *
 * Configuration properties:
 * - whisperModel: Model size (tiny, base, small, medium, large) - default: base
 * - whisperLanguage: Force specific language or auto-detect - default: auto
 * - whisperTimeout: Timeout in seconds for transcription - default: 300
 * - whisperPythonPath: Path to Python executable - default: python3
 */
public final class WhisperPipeline extends AbstractPipeline {

    private static final Set<String> AUDIO_VIDEO_MIME_TYPES = Set.of(
        // Audio formats
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/wave", "audio/x-wav",
        "audio/flac", "audio/ogg", "audio/vorbis", "audio/x-m4a", "audio/mp4",
        "audio/aac", "audio/x-aiff", "audio/webm",
        // Video formats
        "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo",
        "video/x-ms-wmv", "video/x-flv", "video/x-matroska", "video/webm",
        "video/3gpp", "video/mpeg"
    );

    private static final Set<String> AUDIO_VIDEO_EXTENSIONS = Set.of(
        "mp3", "wav", "wma", "aiff", "ogg", "flac", "aac", "m4a", "m3u",
        "avi", "mp4", "mpg", "mov", "wmv", "flv", "mkv", "webm", "3gp", "vob"
    );

    // Whisper supports 99 languages - mapping to Datashare Language enum
    private static final Set<Language> WHISPER_SUPPORTED_LANGUAGES = Set.of(
        Language.ENGLISH, Language.SPANISH, Language.FRENCH, Language.GERMAN,
        Language.ITALIAN, Language.PORTUGUESE, Language.DUTCH, Language.RUSSIAN,
        Language.CHINESE, Language.JAPANESE, Language.KOREAN, Language.ARABIC,
        Language.POLISH, Language.UKRAINIAN, Language.SWEDISH, Language.DANISH,
        Language.NORWEGIAN
        // Whisper supports many more, but limiting to Datashare's Language enum
    );

    private final String whisperModel;
    private final String whisperLanguage;
    private final int whisperTimeout;
    private final String pythonPath;
    private final ObjectMapper objectMapper;

    @Inject
    public WhisperPipeline(final PropertiesProvider propertiesProvider) {
        super(propertiesProvider.getProperties());

        Properties props = propertiesProvider.getProperties();
        this.whisperModel = props.getProperty("whisperModel", "base");
        this.whisperLanguage = props.getProperty("whisperLanguage", "auto");
        this.whisperTimeout = Integer.parseInt(props.getProperty("whisperTimeout", "300"));
        this.pythonPath = props.getProperty("whisperPythonPath", "python3");
        this.objectMapper = new ObjectMapper();

        LOGGER.info("WhisperPipeline initialized with model: {}, language: {}, timeout: {}s",
                    whisperModel, whisperLanguage, whisperTimeout);
    }

    @Override
    public boolean initialize(Language language) throws InterruptedException {
        if (!super.initialize(language)) {
            return false;
        }

        // Verify Python and Whisper are available
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonPath, "-c",
                "import whisper; print(whisper.__version__)");
            Process process = pb.start();
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);

            if (!completed || process.exitValue() != 0) {
                LOGGER.warn("Whisper is not installed or Python is not available. " +
                           "Install with: pip install openai-whisper");
                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            LOGGER.info("Whisper version {} detected", version);
            return true;

        } catch (IOException e) {
            LOGGER.error("Failed to verify Whisper installation", e);
            return false;
        }
    }

    @Override
    public List<NamedEntity> process(Document doc) throws InterruptedException {
        return process(doc, doc.getContentTextLength(), 0);
    }

    @Override
    public List<NamedEntity> process(Document doc, int contentLength, int contentOffset)
            throws InterruptedException {

        // Check if document is an audio/video file
        if (!isAudioVideoFile(doc)) {
            LOGGER.debug("Skipping non-audio/video document: {}", doc.getId());
            return emptyList();
        }

        LOGGER.info("Transcribing audio/video file: {} ({})", doc.getName(), doc.getContentType());

        try {
            String transcription = transcribeFile(doc.getPath());

            if (transcription != null && !transcription.isEmpty()) {
                // Update document content with transcription
                // Note: In a real implementation, this would update the document in Elasticsearch
                LOGGER.info("Successfully transcribed {} ({} characters)",
                           doc.getName(), transcription.length());

                // Whisper doesn't extract named entities, so we return empty list
                // The transcription text is the main output
                return emptyList();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to transcribe file: {}", doc.getPath(), e);
        }

        return emptyList();
    }

    @Override
    public void terminate(Language language) throws InterruptedException {
        super.terminate(language);
        LOGGER.info("WhisperPipeline terminated for language: {}", language);
    }

    @Override
    public Set<Language> supportedLanguages() {
        return WHISPER_SUPPORTED_LANGUAGES;
    }

    /**
     * Check if a document is an audio or video file based on MIME type and extension
     */
    private boolean isAudioVideoFile(Document doc) {
        // Check MIME type
        String contentType = doc.getContentType();
        if (contentType != null && AUDIO_VIDEO_MIME_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }

        // Check file extension
        Path path = doc.getPath();
        if (path != null) {
            String filename = path.getFileName().toString().toLowerCase();
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < filename.length() - 1) {
                String extension = filename.substring(dotIndex + 1);
                return AUDIO_VIDEO_EXTENSIONS.contains(extension);
            }
        }

        return false;
    }

    /**
     * Transcribe an audio/video file using Whisper
     *
     * @param filePath Path to the audio/video file
     * @return Transcribed text
     * @throws IOException if transcription fails
     * @throws InterruptedException if the process is interrupted
     */
    private String transcribeFile(Path filePath) throws IOException, InterruptedException {
        if (filePath == null || !Files.exists(filePath)) {
            LOGGER.warn("File does not exist: {}", filePath);
            return null;
        }

        // Build Python command to run Whisper
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add("-c");

        // Python script to transcribe using Whisper
        String pythonScript = buildWhisperScript(filePath.toString());
        command.add(pythonScript);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Read output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Wait for completion with timeout
        boolean completed = process.waitFor(whisperTimeout, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new IOException("Transcription timed out after " + whisperTimeout + " seconds");
        }

        if (process.exitValue() != 0) {
            throw new IOException("Transcription failed: " + output.toString());
        }

        // Parse JSON output
        try {
            JsonNode jsonNode = objectMapper.readTree(output.toString());
            return jsonNode.get("text").asText();
        } catch (Exception e) {
            LOGGER.error("Failed to parse Whisper output: {}", output.toString(), e);
            // Fallback: return raw output
            return output.toString().trim();
        }
    }

    /**
     * Build Python script to run Whisper transcription
     */
    private String buildWhisperScript(String filePath) {
        StringBuilder script = new StringBuilder();
        script.append("import whisper\n");
        script.append("import json\n");
        script.append("model = whisper.load_model('").append(whisperModel).append("')\n");

        if (!"auto".equals(whisperLanguage)) {
            script.append("result = model.transcribe('").append(filePath)
                  .append("', language='").append(whisperLanguage).append("')\n");
        } else {
            script.append("result = model.transcribe('").append(filePath).append("')\n");
        }

        script.append("print(json.dumps({'text': result['text'], 'language': result.get('language', 'unknown')}))\n");

        return script.toString();
    }
}
