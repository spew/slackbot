package org.poker.stock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachingLogoURLRetriever implements LogoURLRetriever {
    private static final Logger logger = LoggerFactory.getLogger(CachingLogoURLRetriever.class);
    private static final long CACHE_TTL_HOURS = 24;
    private final LogoURLRetriever logoURLRetriever;
    private final LoadingCache<String, Optional<String>> logoURLs;
    private final CacheLoader<String, Optional<String>> cacheLoader = new CacheLoader<String, Optional<String>>() {
        @Override
        public Optional<String> load(String s) throws Exception {
            return logoURLRetriever.retrieve(s);
        }
    };

    public CachingLogoURLRetriever(LogoURLRetriever logoURLRetriever) {
        this.logoURLRetriever = logoURLRetriever;
        this.logoURLs = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(CACHE_TTL_HOURS, TimeUnit.HOURS)
                .build(cacheLoader);
    }

    @Override
    public Optional<String> retrieve(String companyName) {
        try {
            return logoURLs.get(companyName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
