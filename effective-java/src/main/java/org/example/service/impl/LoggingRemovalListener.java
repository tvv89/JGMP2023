package org.example.service.impl;

import org.apache.log4j.Logger;
import org.example.entity.CacheEntry;
import org.example.service.CustomRemovalListener;


public class LoggingRemovalListener implements CustomRemovalListener {

    private static final Logger logger = Logger.getLogger(LoggingRemovalListener.class);
    private Integer numPuts = 0;

    @Override
    public void logRemovedEntry(CacheEntry entry) {
        logger.info("Removed entry from cache: " + entry.getData());
    }

    @Override
    public void logNumEvictions(int numEvictions) {
        logger.info("Number of cache evictions: " + numEvictions);
    }

    @Override
    public void logPutTime(long duration) {
        Long totalPutTime = duration;
        numPuts++;
        logger.info("Average time spent putting new values into the cache: " +
                totalPutTime / numPuts + "ns");
    }
}
