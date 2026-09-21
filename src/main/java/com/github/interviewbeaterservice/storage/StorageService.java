package com.github.interviewbeaterservice.storage;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

/**
 * Граница интеграции с объектным хранилищем (S3 / MinIO).
 * Реализация — {@link S3StorageService}.
 */
public interface StorageService {

    /**
     * Положить объект в хранилище.
     *
     * @param key         storage key (путь внутри бакета)
     * @param content     поток с байтами объекта
     * @param size        размер объекта в байтах
     * @param contentType MIME-тип объекта
     */
    void upload(String key, InputStream content, long size, String contentType);

    /**
     * Удалить объект по ключу. Идемпотентно: ошибки «не найдено» не бросаем.
     */
    void delete(String key);

    /**
     * Сгенерировать временный URL для скачивания объекта.
     */
    URL presignedGetUrl(String key, Duration ttl);
}
