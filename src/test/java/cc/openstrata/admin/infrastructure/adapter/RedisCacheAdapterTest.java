package cc.openstrata.admin.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisCacheAdapterTest {

    private StringRedisTemplate redis;
    private ValueOperations<String, String> ops;
    private RedisCacheAdapter adapter;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        adapter = new RedisCacheAdapter(redis, mapper);
    }

    @Test
    void putSerializesAndStoresWithPrefix() {
        adapter.put("mykey", "myvalue");
        verify(ops).set("admin:mykey", "\"myvalue\"", Duration.ofMinutes(5));
    }

    @Test
    void putStoresInteger() {
        adapter.put("count", 42);
        verify(ops).set("admin:count", "42", Duration.ofMinutes(5));
    }

    @Test
    void getDeserializesAndReturns() {
        when(ops.get("admin:mykey")).thenReturn("\"hello\"");
        String result = adapter.get("mykey", String.class);
        assertEquals("hello", result);
    }

    @Test
    void getReturnsNullWhenMissing() {
        when(ops.get("admin:missing")).thenReturn(null);
        assertNull(adapter.get("missing", String.class));
    }

    @Test
    void containsReturnsTrueWhenKeyExists() {
        when(redis.hasKey("admin:exists")).thenReturn(true);
        assertTrue(adapter.contains("exists"));
    }

    @Test
    void containsReturnsFalseWhenKeyMissing() {
        when(redis.hasKey("admin:missing")).thenReturn(false);
        assertFalse(adapter.contains("missing"));
    }
}
