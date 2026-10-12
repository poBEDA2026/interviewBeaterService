package com.github.interviewbeaterservice.utils;

import com.github.interviewbeaterservice.auth.JwtProperties;
import com.github.interviewbeaterservice.auth.UserPrincipal;
import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Date;

public class TestUtils {

    private static final TemporalAmount TTL = Duration.ofHours(1);
    private static final TemporalAmount RefreshTTL = Duration.ofDays(30L);

    /**
     * Создает дефолтного пользователя
     * @param user пользователь
     * @param id id пользователя
     * @param role роль пользователя
     * @return пользователь
     */
    public static User withId(User user, Long id, Role role) {
        setField(user, "id", id);
        setField(user, "role", role);
        return user;
    }

    /**
     * Изменение поля у объекта
     * @param target объект
     * @param name название поля
     * @param value значение, которое хотим проставить в поле
     */
    public static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Подписать JWT секретом из {@link TestConstants#SECRET}.
     * @param userId subject токена (id пользователя)
     * @param typ значение claim "typ" (ожидаемый тип токена: "access" или "refresh")
     * @param expiry дата истечения токена
     * @return компактная сериализация подписанного JWT
     */
    private static String signToken(long userId, String typ, Date expiry) {
        SecretKey key = Keys.hmacShaKeyFor(TestConstants.SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("typ", typ)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Выпустить валидный access-токен для пользователя по умолчанию.
     * TTL берётся из {@link JwtProperties#ttl()}.
     * @return подписанный JWT с typ="access", subject=DEFAULT_USER_ID
     */
    public static String issueValidAccessToken() {
        return signToken(TestConstants.DEFAULT_USER_ID,
                TestConstants.CLAIM_TYPE_ACCESS,
                Date.from(Instant.now().plus(TTL)));
    }

    /**
     * Выпустить просроченный access-токен для пользователя по умолчанию.
     * Срок истечения — 60 секунд назад, поэтому при проверке JwtService бросит ExpiredJwtException.
     * @return подписанный JWT с typ="access" и истёкшим сроком
     */
    public static String issueExpiredAccessToken() {
        return signToken(TestConstants.DEFAULT_USER_ID,
                TestConstants.CLAIM_TYPE_ACCESS,
                Date.from(Instant.now().minusSeconds(60)));
    }

    /**
     * Выпустить валидный refresh-токен для пользователя по умолчанию.
     * TTL берётся из {@link JwtProperties#refreshTtl()}.
     * @return подписанный JWT с typ="refresh", subject=DEFAULT_USER_ID
     */
    public static String issueRefreshToken() {
        return signToken(TestConstants.DEFAULT_USER_ID,
                TestConstants.CLAIM_TYPE_REFRESH,
                Date.from(Instant.now().plus(RefreshTTL)));
    }

    /**
     * Собрать {@link UserPrincipal} для теста: создаёт {@link User} с дефолтным email/паролем
     * и ролью {@link Role#USER}, проставляет ему id через reflection (поле {@code id}
     * @param id id пользователя, который нужно выставить
     * @return UserPrincipal поверх только что собранного User
     */
    public static UserPrincipal principalFor(long id) {
        User u = User.builder()
                .email(TestConstants.DEFAULT_EMAIL)
                .password("x")
                .build();

        TestUtils.withId(u, id, Role.USER);
        return new UserPrincipal(u);
    }
}
