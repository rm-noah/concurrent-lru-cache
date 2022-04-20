package com.noah;

import java.util.*;
import java.util.concurrent.*;
import org.junit.*;

public class MapTest {

    @Test
    public void testBasicStructure() {
        ConcurrentLRUCache<Integer> cache = new ConcurrentLRUCache<>(5);

        cache.add(1);
        cache.add(2);
        cache.add(3);
        cache.add(4);
        cache.add(5);
        cache.add(6);

        Assert.assertEquals(5, cache.size());
        Assert.assertTrue(cache.find(1).isEmpty());
        Assert.assertNotNull(cache.find(6));
    }

    @Test
    public void testReordering() {
        ConcurrentLRUCache<Integer> cache = new ConcurrentLRUCache<>(5);

        cache.add(1);
        cache.add(2);
        cache.add(3);
        cache.add(4);
        cache.add(5);

        cache.find(1);

        cache.add(6);

        Assert.assertEquals(5, cache.size());
        Assert.assertTrue(cache.find(2).isEmpty());
        Assert.assertFalse(cache.find(1).isEmpty());
    }

    @Test
    public void testRemovingLeastInterestingValue() {
        ConcurrentLRUCache<Integer> cache = new ConcurrentLRUCache<>(5);

        cache.add(1);
        cache.add(2);
        cache.add(3);
        cache.add(4);
        cache.add(5);

        cache.find(1);
        cache.find(1);
        cache.find(1);

        cache.add(6);

        Assert.assertFalse(cache.find(2).isPresent());
        Assert.assertEquals(4, cache.getPopularity(1));
        // Find increments "hits" count, order of operations is important here
        Assert.assertTrue(cache.find(1).isPresent());
    }

    @Test
    public void concurrencyTest() throws InterruptedException {
        ConcurrentLRUCache<Integer> cache = new ConcurrentLRUCache<>(5);

        final Callable<String> task = () -> {
            cache.add(1);
            cache.add(2);
            cache.add(3);
            cache.add(4);
            cache.add(5);
            cache.add(6);
            cache.add(7);
            cache.add(8);

            return "Executed Callable task";
        };

        List<Callable<String>> tasks = Arrays.asList(task, task, task);
        ExecutorService service = Executors.newFixedThreadPool(5);
        service.invokeAll(tasks);

        cache.add(1);
        cache.add(2);
        cache.add(3);
        cache.add(4);
        cache.add(5);
        cache.add(6);
        cache.add(7);
        cache.add(8);

        Assert.assertFalse(cache.find(1).isPresent());
        Assert.assertFalse(cache.find(2).isPresent());
        Assert.assertFalse(cache.find(3).isPresent());
        Assert.assertTrue(cache.find(4).isPresent());
        Assert.assertTrue(cache.find(5).isPresent());
        Assert.assertTrue(cache.find(6).isPresent());
        Assert.assertTrue(cache.find(7).isPresent());
        Assert.assertTrue(cache.find(8).isPresent());
    }
}
