package com.github.interviewbeaterservice.question.service;

import com.github.interviewbeaterservice.question.dto.AttachmentResponse;
import com.github.interviewbeaterservice.question.entity.Attachment;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.exception.AttachmentNotFoundException;
import com.github.interviewbeaterservice.question.exception.InvalidAttachmentException;
import com.github.interviewbeaterservice.question.exception.QuestionNotFoundException;
import com.github.interviewbeaterservice.question.repository.AttachmentRepository;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.storage.StorageProperties;
import com.github.interviewbeaterservice.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionAttachmentServiceTest {

    @Mock QuestionRepository questionRepository;
    @Mock AttachmentRepository attachmentRepository;
    @Mock StorageService storageService;

    StorageProperties properties;

    QuestionAttachmentService service;

    @BeforeEach
    void setUp() {
        properties = new StorageProperties(
                "http://localhost:9000",
                "us-east-1",
                "bucket",
                "ak",
                "sk",
                20L * 1024 * 1024,
                10,
                Set.of("image/png", "image/jpeg", "application/pdf", "text/plain"),
                Duration.ofMinutes(15)
        );
        service = new QuestionAttachmentService(
                questionRepository, attachmentRepository, storageService, properties);
    }

    private static Question question(long id) {
        Question q = new Question();
        setField(q, "id", id);
        return q;
    }

    private static Attachment attachment(long id) {
        Attachment a = Attachment.builder()
                .storageKey("k").originalName("n").contentType("text/plain").sizeBytes(1)
                .build();
        setField(a, "id", id);
        return a;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MultipartFile pngFile(String name) {
        return new MockMultipartFile(
                "files", name, "image/png", new byte[]{1, 2, 3, 4});
    }

    @Test
    void upload_happyPath_savesAndUploads() {
        Question q = question(7L);
        when(questionRepository.findById(7L)).thenReturn(Optional.of(q));
        when(attachmentRepository.save(any(Attachment.class)))
                .thenAnswer(inv -> {
                    Attachment a = inv.getArgument(0);
                    setField(a, "id", 99L);
                    return a;
                });

        List<AttachmentResponse> result = service.upload(7L, List.of(pngFile("pic.png")));

        assertThat(result).hasSize(1);
        AttachmentResponse r = result.get(0);
        assertThat(r.id()).isEqualTo(99L);
        assertThat(r.originalName()).isEqualTo("pic.png");
        assertThat(r.contentType()).isEqualTo("image/png");
        assertThat(r.sizeBytes()).isEqualTo(4);

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        verify(storageService).upload(keyCap.capture(), any(InputStream.class),
                longThat(s -> s == 4L), eq("image/png"));
        assertThat(keyCap.getValue()).startsWith("questions/7/").endsWith(".png");
    }

    @Test
    void upload_noFiles_throwsInvalidAttachment() {
        assertThatThrownBy(() -> service.upload(1L, List.of()))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("At least one file");
    }

    @Test
    void upload_tooManyFiles_throwsInvalidAttachment() {
        List<MultipartFile> files = List.of(
                pngFile("a.png"), pngFile("b.png"), pngFile("c.png"));
        StorageProperties tight = new StorageProperties(
                "http://localhost:9000", "us-east-1", "bucket", "ak", "sk",
                20L * 1024 * 1024, 2, Set.of("image/png"), Duration.ofMinutes(15));
        QuestionAttachmentService s = new QuestionAttachmentService(
                questionRepository, attachmentRepository, storageService, tight);

        assertThatThrownBy(() -> s.upload(1L, files))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("Too many files");
        verifyNoInteractions(storageService);
        verifyNoInteractions(attachmentRepository);
    }

    @Test
    void upload_oversizeFile_throwsInvalidAttachment() {
        StorageProperties tight = new StorageProperties(
                "http://localhost:9000", "us-east-1", "bucket", "ak", "sk",
                3, 10, Set.of("image/png"), Duration.ofMinutes(15));
        QuestionAttachmentService s = new QuestionAttachmentService(
                questionRepository, attachmentRepository, storageService, tight);

        assertThatThrownBy(() -> s.upload(1L, List.of(pngFile("big.png"))))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("too large");
        verifyNoInteractions(storageService);
    }

    @Test
    void upload_disallowedContentType_throwsInvalidAttachment() {
        Question q = question(1L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));

        MockMultipartFile html = new MockMultipartFile(
                "files", "page.html", "text/html", "<html/>".getBytes());

        assertThatThrownBy(() -> service.upload(1L, List.of(html)))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("not allowed");
        verifyNoInteractions(storageService);
    }

    @Test
    void upload_questionNotFound_throwsQuestionNotFound() {
        when(questionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(404L, List.of(pngFile("a.png"))))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void list_returnsMappedDtos() {
        when(questionRepository.existsById(1L)).thenReturn(true);
        Attachment a = attachment(5L);
        when(attachmentRepository.findAllByQuestionId(1L)).thenReturn(List.of(a));

        List<AttachmentResponse> out = service.list(1L);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).id()).isEqualTo(5L);
        assertThat(out.get(0).originalName()).isEqualTo("n");
    }

    @Test
    void list_questionNotFound_throwsQuestionNotFound() {
        when(questionRepository.existsById(7L)).thenReturn(false);
        assertThatThrownBy(() -> service.list(7L))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void presignedDownloadUrl_returnsUrlFromStorage() throws Exception {
        Attachment a = attachment(11L);
        when(attachmentRepository.findByQuestionIdAndId(1L, 11L))
                .thenReturn(Optional.of(a));
        URL expected = URI.create("http://localhost:9000/bucket/k?sig=abc").toURL();
        when(storageService.presignedGetUrl(eq("k"), eq(Duration.ofMinutes(15))))
                .thenReturn(expected);

        URL out = service.presignedDownloadUrl(1L, 11L);
        assertThat(out).isEqualTo(expected);
    }

    @Test
    void presignedDownloadUrl_attachmentNotFound_throws() {
        when(attachmentRepository.findByQuestionIdAndId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.presignedDownloadUrl(1L, 99L))
                .isInstanceOf(AttachmentNotFoundException.class);
    }

    @Test
    void delete_callsStorageThenRepo() {
        Attachment a = attachment(11L);
        when(attachmentRepository.findByQuestionIdAndId(1L, 11L))
                .thenReturn(Optional.of(a));

        service.delete(1L, 11L);

        InOrder order = inOrder(storageService, attachmentRepository);
        order.verify(storageService).delete("k");
        order.verify(attachmentRepository).delete(a);
    }

    @Test
    void delete_attachmentNotFound_throws() {
        when(attachmentRepository.findByQuestionIdAndId(1L, 11L))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(1L, 11L))
                .isInstanceOf(AttachmentNotFoundException.class);
        verifyNoInteractions(storageService);
    }

    @Test
    void buildKey_isDefensiveAgainstPathTraversal() {
        Question q = question(1L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(attachmentRepository.save(any(Attachment.class)))
                .thenAnswer(inv -> { Attachment a = inv.getArgument(0); setField(a, "id", 1L); return a; });

        MockMultipartFile evil = new MockMultipartFile(
                "files", "../../etc/passwd", "image/png", new byte[]{0});
        service.upload(1L, List.of(evil));

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        verify(storageService).upload(keyCap.capture(), any(InputStream.class),
                longThat(s -> s == 1L), eq("image/png"));
        assertThat(keyCap.getValue()).doesNotContain("..").doesNotContain("/etc/");
    }
}
