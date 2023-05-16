package org.example.service;

import org.example.entity.CacheEntry;

public interface CustomRemovalListener {
    void logRemovedEntry(CacheEntry entry);
    void logNumEvictions(int numEvictions);
    void logPutTime(long duration);
}
