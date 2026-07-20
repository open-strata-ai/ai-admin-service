package cc.openstrata.admin.infrastructure.adapter;

import cc.openstrata.admin.domain.port.CachePort;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** In-memory CachePort standing in for Redis/Valkey (resource view cache). Offline default. */
@Component
@Profile("!prod")
public class InMemoryCacheAdapter implements CachePort {

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value) {
        store.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) store.get(key);
    }

    @Override
    public boolean contains(String key) {
        return store.containsKey(key);
    }
}
