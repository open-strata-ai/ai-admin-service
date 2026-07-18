package com.openstrata.admin.infrastructure.adapter;

import com.openstrata.admin.domain.port.CachePort;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory CachePort standing in for Redis/Valkey (resource view cache). */
@Component
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
