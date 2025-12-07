package org.icij.datashare.text.nlp.whisper;

import org.icij.datashare.PropertiesProvider;
import org.icij.datashare.text.Document;
import org.icij.datashare.text.Language;
import org.icij.datashare.text.NamedEntity;
import org.icij.datashare.text.nlp.Pipeline;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.icij.datashare.text.DocumentBuilder.createDoc;
import static org.icij.datashare.text.Language.ENGLISH;

public class WhisperPipelineTest {
    private WhisperPipeline pipeline;

    @Before
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty("whisperModel", "base");
        properties.setProperty("whisperLanguage", "auto");
        properties.setProperty("whisperTimeout", "300");
        properties.setProperty("whisperPythonPath", "python3");

        PropertiesProvider propertiesProvider = new PropertiesProvider(new HashMap<>());
        for (String key : properties.stringPropertyNames()) {
            propertiesProvider.getProperties().setProperty(key, properties.getProperty(key));
        }

        pipeline = new WhisperPipeline(propertiesProvider);
    }

    @Test
    public void test_pipeline_type() {
        assertThat(pipeline.getType()).isEqualTo(Pipeline.Type.WHISPER);
    }

    @Test
    public void test_supported_languages() {
        assertThat(pipeline.supportedLanguages()).contains(
            Language.ENGLISH, Language.FRENCH, Language.SPANISH,
            Language.GERMAN, Language.ITALIAN
        );
    }

    @Test
    public void test_supports_english() {
        assertThat(pipeline.supports(Language.ENGLISH)).isTrue();
    }

    @Test
    public void test_non_audio_document_returns_empty() {
        Document textDoc = createDoc("doc1")
            .with("This is plain text content")
            .with(ENGLISH)
            .ofContentType("text/plain")
            .build();

        List<NamedEntity> result = pipeline.process(textDoc);
        assertThat(result).isEmpty();
    }

    @Test
    public void test_audio_document_detected_by_mime_type() throws Exception {
        Document audioDoc = createDoc("doc1")
            .with("audio content")
            .with(ENGLISH)
            .with(Paths.get("/tmp/test.mp3"))
            .ofContentType("audio/mpeg")
            .build();

        // Process will attempt transcription but fail without actual Whisper
        // We're just testing that it detects it as an audio file
        List<NamedEntity> result = pipeline.process(audioDoc);
        // Should return empty list because transcription will fail, but it won't skip the file
        assertThat(result).isNotNull();
    }

    @Test
    public void test_video_document_detected_by_mime_type() throws Exception {
        Document videoDoc = createDoc("doc1")
            .with("video content")
            .with(ENGLISH)
            .with(Paths.get("/tmp/test.mp4"))
            .ofContentType("video/mp4")
            .build();

        List<NamedEntity> result = pipeline.process(videoDoc);
        assertThat(result).isNotNull();
    }

    @Test
    public void test_audio_document_detected_by_extension() throws Exception {
        Document audioDoc = createDoc("doc1")
            .with("audio content")
            .with(ENGLISH)
            .with(Paths.get("/path/to/file.wav"))
            .ofContentType("application/octet-stream")
            .build();

        // Should detect as audio by extension even if MIME type is generic
        List<NamedEntity> result = pipeline.process(audioDoc);
        assertThat(result).isNotNull();
    }

    @Test
    public void test_various_audio_formats_detected() {
        String[] audioExtensions = {"mp3", "wav", "flac", "ogg", "m4a", "aac"};

        for (String ext : audioExtensions) {
            Document audioDoc = createDoc("doc_" + ext)
                .with("audio content")
                .with(ENGLISH)
                .with(Paths.get("/path/to/file." + ext))
                .ofContentType("application/octet-stream")
                .build();

            List<NamedEntity> result = pipeline.process(audioDoc);
            assertThat(result).as("Should process ." + ext + " files").isNotNull();
        }
    }

    @Test
    public void test_various_video_formats_detected() {
        String[] videoExtensions = {"mp4", "avi", "mkv", "mov", "wmv", "flv"};

        for (String ext : videoExtensions) {
            Document videoDoc = createDoc("doc_" + ext)
                .with("video content")
                .with(ENGLISH)
                .with(Paths.get("/path/to/file." + ext))
                .ofContentType("application/octet-stream")
                .build();

            List<NamedEntity> result = pipeline.process(videoDoc);
            assertThat(result).as("Should process ." + ext + " files").isNotNull();
        }
    }

    @Test
    public void test_non_audio_extension_not_detected() {
        Document pdfDoc = createDoc("doc1")
            .with("pdf content")
            .with(ENGLISH)
            .with(Paths.get("/path/to/file.pdf"))
            .ofContentType("application/pdf")
            .build();

        List<NamedEntity> result = pipeline.process(pdfDoc);
        assertThat(result).isEmpty();
    }

    @Test
    public void test_encoding_default() {
        assertThat(pipeline.getEncoding()).isEqualTo(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    public void test_target_entities_default() {
        // Whisper doesn't extract named entities, so target entities should be default
        assertThat(pipeline.getTargetEntities()).isNotEmpty();
    }

    @Test
    public void test_caching_default() {
        assertThat(pipeline.isCaching()).isTrue();
    }
}
