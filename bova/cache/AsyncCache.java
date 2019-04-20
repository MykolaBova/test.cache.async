package com.bova.cache;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class AsyncCache<K, V> {

    private volatile ConcurrentHashMap<Key, V> globalMap = new ConcurrentHashMap<Key, V>();
    private long expiration;

    AsyncCache(long expiration) {
        this.expiration = expiration;
    }

    CompletableFuture<V> getOrCompute(K key,
                                      Function<K,    CompletableFuture<V>> supplier)
            throws ExecutionException, InterruptedException {
        removeExpiredElements();

        CompletableFuture<V> completableFuture = new CompletableFuture<>();

        V v = get(key);

        if(v == null) {
            CompletableFuture<V> supplierFuture = supplier.apply(key);
            v = supplierFuture.get();
        }

        completableFuture.complete(v);

        return completableFuture;
    }

    boolean containsKey(K key) {
        removeExpiredElements();

        V value = get(key);
        return value != null;
    }

    public void put(K key, V data) {
        globalMap.put(new Key(key, expiration), data);
    }

    public V get(K key) {
        return globalMap.get(new Key(key, expiration));
    }

    public void remove(K key) {
        globalMap.remove(new Key(key, expiration));
    }

    private void removeExpiredElements() {
        long current = System.currentTimeMillis();

        for (Key k : globalMap.keySet()) {
            if (!k.isLive(current)) {
                globalMap.remove(k);
            }
        }
    }

    private static class Key {

        private final Object key;
        private final long timelife;

        Key(Object key, long timeout) {
            this.key = key;
            this.timelife = System.currentTimeMillis() + timeout;
        }

        public boolean isLive(long currentTimeMillis) {
            return currentTimeMillis < timelife;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            return Objects.equals(this.key, other.key);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + (this.key != null ? this.key.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "Key{" + "key=" + key + '}';
        }
    }
}
