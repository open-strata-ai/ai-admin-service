package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.port.CachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis-backed CachePort for prod profile (SPRING_PROFILES_ACTIVE=prod). */
@Component
@Profile("prod")
public class RedisCacheAdapter implements CachePort {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private static final Duration TTL = Duration.ofMinutes(5);

    public RedisCacheAdapter(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public void put(String key, Object value) {
        try {
            redis.opsForValue().set(prefix(key), mapper.writeValueAsString(value), TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cache value for key: " + key, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        String raw = redis.opsForValue().get(prefix(key));
        if (raw == null) return null;
        try {
            return mapper.readValue(raw, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize cache value for key: " + key, e);
        }
    }

    @Override
    public boolean contains(String key) {
        return Boolean.TRUE.equals(redis.hasKey(prefix(key)));
    }

    private static String prefix(String key) {
        return "admin:" + key;
    }
}
