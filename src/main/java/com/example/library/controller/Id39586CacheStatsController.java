package com.example.library.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/cache")
public class Id39586CacheStatsController {

    private final CacheManager cacheManager;

    public Id39586CacheStatsController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> allStats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                Cache<Object, Object> nativeCache = ((CaffeineCache) cache).getNativeCache();
                CacheStats stats = nativeCache.stats();

                Map<String, Object> statsMap = new HashMap<>();
                statsMap.put("hitCount", stats.hitCount());
                statsMap.put("missCount", stats.missCount());
                statsMap.put("hitRate", stats.hitRate());
                statsMap.put("evictionCount", stats.evictionCount());
                statsMap.put("loadSuccessCount", stats.loadSuccessCount());
                statsMap.put("loadFailureCount", stats.loadFailureCount());
                statsMap.put("estimatedSize", nativeCache.estimatedSize());

                allStats.put(cacheName, statsMap);
            }
        });

        return allStats;
    }

    @GetMapping("/clear")
    public Map<String, String> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All caches cleared");
        return response;
    }
}