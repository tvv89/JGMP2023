package org.example.service;

import org.example.entity.CacheEntry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService {
    private static final int MAX_SIZE = 100000;
    private static final int EVICTION_TIME_SECONDS = 5;

    private final ConcurrentHashMap<String, CacheEntry> cache;
    private final LinkedHashMap<String, Long> accessTimes;
    private final CustomRemovalListener removalListener;

    public CacheService(CustomRemovalListener removalListener) {
        this.cache = new ConcurrentHashMap<>();
        this.accessTimes = new LinkedHashMap<>();
        this.removalListener = removalListener;
    }

    public void put(String key, String data) {
        long startTime = System.nanoTime();
        removeExpiredEntries();
        CacheEntry entry = new CacheEntry(data);
        CacheEntry oldEntry = cache.put(key, entry);
        if (oldEntry != null) {
            accessTimes.remove(key);
        }
        accessTimes.put(key, System.currentTimeMillis());
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        removalListener.logPutTime(duration);
        if (cache.size() > MAX_SIZE) {
            evictEntries();
        }
    }

    public Map<String, CacheEntry> getCache() {
        return cache;
    }

    public CacheEntry get(String key) {
        removeExpiredEntries();
        CacheEntry entry = cache.get(key);
        if (entry != null) {
            accessTimes.put(key, System.currentTimeMillis());
        }
        return entry;
    }

    private void evictEntries() {
        int numEvictions = 0;
        Iterator<Map.Entry<String, Long>> iterator = accessTimes.entrySet().iterator();
        while (iterator.hasNext() && cache.size() > MAX_SIZE) {
            Map.Entry<String, Long> entry = iterator.next();
            String key = entry.getKey();
            CacheEntry removedEntry = cache.remove(key);
            if (removedEntry != null) {
                removalListener.logRemovedEntry(removedEntry);
                numEvictions++;
            }
            iterator.remove();
        }
        removalListener.logNumEvictions(numEvictions);
    }

    private void removeExpiredEntries() {
        Iterator<Map.Entry<String, Long>> iterator = accessTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String key = entry.getKey();
            Long accessTime = entry.getValue();
            if (System.currentTimeMillis() - accessTime >= EVICTION_TIME_SECONDS * 1000) {
                iterator.remove();
                removalListener.logRemovedEntry(cache.get(key));
                cache.remove(key);
            }
        }
    }
}