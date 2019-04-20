/*
Resources:

1) Implementing Lazy Initialization
https://dzone.com/articles/be-lazy-with-java-8

2) Пишем кеш с определенным временем хранения объектов с использованием java.util.concurrent
https://habr.com/ru/post/140214/

 */
package com.bova.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args)
            throws ExecutionException, InterruptedException {
        System.out.println("Async Cache");

        AsyncCache<String, String> asyncCache = new AsyncCache<>(1000);
        asyncCache.put("1", "11");
        asyncCache.put("2", "12");
        asyncCache.remove("2");
        System.out.println("get(1) " + asyncCache.get("1"));
        System.out.println("get(3) " + asyncCache.get("3"));
        System.out.println("get(null) " + asyncCache.get(null));
        System.out.println("Key 1 exists? "+asyncCache.containsKey("1"));
        System.out.println("Key 2 exists? "+asyncCache.containsKey("2"));

        System.out.println("getOrCompute(1) "+asyncCache.getOrCompute("1",
                x -> {
                    String v = "Value for key" + x;
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    completableFuture.complete(v);
                    return completableFuture;
                }).get()
        );

        System.out.println("getOrCompute(2) "+asyncCache.getOrCompute("2",
                x -> {
                    String v = "Value for key " + x;
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    completableFuture.complete(v);
                    return completableFuture;
                }).get()
        );

        TimeUnit.SECONDS.sleep(2);

        System.out.println("Key 1 exists after sleep? " + asyncCache.containsKey("1"));
    }
}
/*
Async Cache
get(1) 11
get(3) null
get(null) null
Key 1 exists? true
Key 2 exists? false
getOrCompute(1) 11
getOrCompute(2) Value for key 2
Key 1 exists after sleep? false

Process finished with exit code 0
 */
