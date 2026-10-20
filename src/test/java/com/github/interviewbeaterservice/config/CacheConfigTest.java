package com.github.interviewbeaterservice.config;

import com.github.interviewbeaterservice.answer.dto.AnswerResponse;
import com.github.interviewbeaterservice.question.dto.QuestionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CacheConfigTest {

    @Test
    void configClassIsAnnotatedWithEnableCaching() {
        assertThat(CacheConfig.class.isAnnotationPresent(EnableCaching.class)).isTrue();
    }

    @Test
    void questionsCacheHasOneHourTtlAndJsonSerialization() {
        Cache cache = cache(CacheConfig.QUESTIONS_CACHE);
        assertThat(cache).isInstanceOf(RedisCache.class);
        RedisCacheConfiguration questions = ((RedisCache) cache).getCacheConfiguration();
        assertThat(questions.getTtlFunction().getTimeToLive("k", "v"))
                .isEqualTo(Duration.ofHours(1));
        assertThat(questions.getAllowCacheNullValues()).isFalse();
    }

    /** Менеджер зарегистрирован как @Bean, поэтому конфигурации кэшей загружаются контейнером, а не конструктором */
    @Test
    void managerRegistersSeparateCachesForQuestionsAndAnswers() {
        RedisCacheManager manager = manager();

        assertThat(manager.getCacheNames()).containsExactlyInAnyOrder(
                CacheConfig.QUESTIONS_CACHE, CacheConfig.ANSWERS_CACHE);
    }

    private static RedisCacheManager manager() {
        RedisCacheManager manager = new CacheConfig().cacheManager(mock(RedisConnectionFactory.class));
        manager.initializeCaches();
        return manager;
    }

    private static RedisCache cache(String cacheName) {
        return (RedisCache) manager().getCache(cacheName);
    }

    @Test
    void questionsCacheValuesRoundTripThroughJson() {
        RedisSerializationContext.SerializationPair<Object> values = valuePair(CacheConfig.QUESTIONS_CACHE);

        QuestionResponse response = new QuestionResponse(42L, "Что такое ACID?", Instant.EPOCH);
        ByteBuffer encoded = values.getWriter().write(response);

        assertThat(values.getReader().read(encoded)).isEqualTo(response);
    }

    /** Каждый кэш сериализует значения своего типа: в questions лежит QuestionResponse, в answers — AnswerResponse */
    @Test
    void eachCacheRoundTripsValuesOfItsOwnType() {
        RedisSerializationContext.SerializationPair<Object> questions = valuePair(CacheConfig.QUESTIONS_CACHE);
        RedisSerializationContext.SerializationPair<Object> answers = valuePair(CacheConfig.ANSWERS_CACHE);

        QuestionResponse question = new QuestionResponse(42L, "Что такое ACID?", Instant.EPOCH);
        AnswerResponse answer = new AnswerResponse(7L, "Транзакция", "ACID атомарна", true);

        assertThat(questions.getReader().read(questions.getWriter().write(question))).isEqualTo(question);
        assertThat(answers.getReader().read(answers.getWriter().write(answer))).isEqualTo(answer);
    }

    private static RedisSerializationContext.SerializationPair<Object> valuePair(String cacheName) {
        // Кэш хранит значения своего типа, поэтому его пара сериализаторов не параметризована.
        @SuppressWarnings("unchecked")
        RedisSerializationContext.SerializationPair<Object> pair =
                (RedisSerializationContext.SerializationPair<Object>)
                        cache(cacheName).getCacheConfiguration().getValueSerializationPair();
        return pair;
    }

    /** Кэш хранит значения разных типов, поэтому его пара сериализаторов не параметризована */
    @SuppressWarnings("unchecked")
    private static RedisSerializationContext.SerializationPair<Object> valuePair(RedisCache cache) {
        return (RedisSerializationContext.SerializationPair<Object>)
                cache.getCacheConfiguration().getValueSerializationPair();
    }

    @Test
    void failOpenErrorHandlerSwallowsCacheFailures() {
        CacheConfig config = new CacheConfig();
        CacheErrorHandler handler = config.failOpenCacheErrorHandler();

        Cache cache = mock(Cache.class);

        // Исключения ни из одного колбэка не должны наружу пробрасываться.
        handler.handleCacheGetError(new RuntimeException("redis down"), cache, "k");
        handler.handleCachePutError(new RuntimeException("redis down"), cache, "k", "v");
        handler.handleCacheEvictError(new RuntimeException("redis down"), cache, "k");
        handler.handleCacheClearError(new RuntimeException("redis down"), cache);
    }
}