package com.github.interviewbeaterservice.config;

import com.github.interviewbeaterservice.answer.dto.AnswerResponse;
import com.github.interviewbeaterservice.question.dto.QuestionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;

/**
 * Конфигурация Spring Cache поверх Redis
 * <p>
 * Кэшируются только плоские DTO: JPA-entity с ленивыми связями в Redis класть нельзя,
 * они не сериализуются и ломают последующие запросы
 * <p>
 * При недоступности Redis работа не падает — см. {@link #failOpenCacheErrorHandler()}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Имя кэша, в котором лежат вопросы. */
    public static final String QUESTIONS_CACHE = "questions";

    /** Имя кэша, в котором лежат варианты ответов. */
    public static final String ANSWERS_CACHE = "answers";

    /** Время жизни записи в кэше. */
    public static final Duration TTL = Duration.ofHours(1);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration(valueSerializer(String.class)))
                .withInitialCacheConfigurations(Map.of(
                        QUESTIONS_CACHE, cacheConfiguration(valueSerializer(QuestionResponse.class)),
                        ANSWERS_CACHE, cacheConfiguration(valueSerializer(AnswerResponse.class))
                ))
                .build();
    }

    private static RedisCacheConfiguration cacheConfiguration(
            RedisSerializationContext.SerializationPair<?> values) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(values);
    }

    /**
     * JSON-сериализатор, зафиксированный на одном типе значений.
     * <p>
     * Тип задан явно, а не подставляется из разбираемого JSON: в Redis лежит чистый JSON
     * без {@code @class}, и на чтении возвращается ровно объявленный тип.
     * {@code GenericJacksonJsonRedisSerializer} здесь не подходит — тому нужен default typing
     * по базовому типу {@link Object}, то есть он пишет в Redis имя класса и при чтении
     * доверяет ему, десериализуя произвольный тип из кэша.
     * <p>
     * В Jackson 3 поддержка {@code java.time} встроена в ядро, а даты по умолчанию пишутся
     * в ISO-формате, поэтому отдельный модуль и настройки не нужны.
     * <p>
     * {@link JacksonJsonRedisSerializer} сам не отвечает {@code canSerialize}, а кэш спрашивает
     * именно об этом, поэтому пара собирается вручную: {@code byteArray()} пропускает байты
     * как есть, а {@code just(...)} навешивает сверху Jackson — {@code canSerialize} тут всегда истина.
     */
    private static <T> RedisSerializationContext.SerializationPair<T> valueSerializer(Class<T> type) {
        JacksonJsonRedisSerializer<T> json = new JacksonJsonRedisSerializer<>(JsonMapper.builder().build(), type);

        return RedisSerializationContext.SerializationPair.just(
                buffer -> {
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    return json.deserialize(bytes);
                },
                payload -> ByteBuffer.wrap(json.serialize(payload))
        );
    }

    /**
     * Проглатывает ошибки кэша: если Redis недоступен, приложение продолжает работать,
     * просто без кэша.
     */
    @Bean
    public CacheErrorHandler failOpenCacheErrorHandler() {
        return new FailOpenCacheErrorHandler();
    }

    @Slf4j
    static class FailOpenCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Ошибка чтения из кэша {} по ключу {}: {}", cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Ошибка записи в кэш {} по ключу {}: {}", cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Ошибка инвалидации кэша {} по ключу {}: {}", cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Ошибка очистки кэша {}: {}", cache.getName(), exception.getMessage());
        }
    }
}