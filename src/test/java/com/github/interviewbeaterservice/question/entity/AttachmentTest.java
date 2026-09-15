package com.github.interviewbeaterservice.question.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentTest {

    @Test
    void builder_setsAllContentFields() {
        Attachment a = Attachment.builder()
                .storageKey("questions/1/abc.pdf")
                .originalName("task.pdf")
                .contentType("application/pdf")
                .sizeBytes(12345L)
                .build();

        assertThat(a.getStorageKey()).isEqualTo("questions/1/abc.pdf");
        assertThat(a.getOriginalName()).isEqualTo("task.pdf");
        assertThat(a.getContentType()).isEqualTo("application/pdf");
        assertThat(a.getSizeBytes()).isEqualTo(12345L);
        assertThat(a.getId()).isNull();
        assertThat(a.getUploadedAt()).isNull();
        assertThat(a.getQuestion()).isNull();
    }

    @Test
    void onCreate_setsUploadedAt() throws Exception {
        Attachment a = Attachment.builder()
                .storageKey("k").originalName("n").contentType("text/plain").sizeBytes(1)
                .build();

        invokePrivate(a, "onCreate");

        assertThat(a.getUploadedAt()).isNotNull();
        assertThat(a.getUploadedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void onCreate_doesNotOverwriteExistingUploadedAt() throws Exception {
        Instant pre = Instant.parse("2020-01-01T00:00:00Z");
        Attachment a = Attachment.builder()
                .storageKey("k").originalName("n").contentType("text/plain").sizeBytes(1)
                .build();
        a.setUploadedAt(pre);

        invokePrivate(a, "onCreate");

        assertThat(a.getUploadedAt()).isEqualTo(pre);
    }

    private static void invokePrivate(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }
}
