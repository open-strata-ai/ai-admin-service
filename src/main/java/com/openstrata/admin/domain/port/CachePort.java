package com.openstrata.admin.domain.port;

/**
 * Cache SPI (§4.3.4). Redis is default; Valkey is an alternative (§16.3). Both
 * implement this same port so switching requires zero domain changes.
 */
public interface CachePort {
    void put(String key, Object value);
    <T> T get(String key, Class<T> type);
    boolean contains(String key);
}
