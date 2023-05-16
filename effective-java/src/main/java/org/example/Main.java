package org.example;

import com.google.common.cache.*;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.example.entity.CacheEntry;
import org.example.service.CacheService;
import org.example.service.CustomRemovalListener;
import org.example.service.impl.LoggingRemovalListener;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    public static void main(String[] args) {

        //Simple Java (Strategy: LFU);
        CustomRemovalListener removalListener = new LoggingRemovalListener();
        CacheService cacheService = new CacheService(removalListener);
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");
        cacheService.put("key3", "value3");
        CacheEntry entry = cacheService.get("key1");
        if (entry != null) {
            System.out.println("Found entry in cache: " + entry.getData());
        } else {
            System.out.println("Entry not found in cache");
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cacheService.put("key4", "value4");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cacheService.put("key5", "value5");

        for (Map.Entry<String, CacheEntry> cacheEntry : cacheService.getCache().entrySet()) {
            System.out.println(cacheEntry.getKey() + " -> " + cacheEntry.getValue().getData());
        }


        //Guava (Strategy: LRU).
        CacheLoader<String, String> loader;
        loader = new CacheLoader<>() {
            @Override
            public String load(@NonNull String key) {
                return key;
            }
        };
        LoadingCache<String, String> cache;
        RemovalListener<String, String> rl = notification -> logger.info(notification.getKey() + "->" + notification.getValue());
        cache = CacheBuilder.newBuilder()
                .maximumSize(100000)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .removalListener(rl)
                .build(loader);
        cache.put("key1", "value1");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cacheService.put("key2", "value2");
    }
}