package com.noah;

import java.util.*;
import java.util.concurrent.*;
import org.junit.*;

public class MapTest {

    @Test
    public void testBasicStructure() {
        ForgettingMap cache = new ForgettingMap(5);

        cache.add(1);
        cache.add(2);
        cache.add(3);
        cache.add(4);
        cache.add(5);
        cache.add(6);

        Assert.assertEquals(cache.size(), 5);
        Assert.assertTrue(cache.find(1).isEmpty());
        Assert.assertNotNull(cache.find(6));
    }

    @Test
    public void testReordering() {
        ForgettingMap cache = new ForgettingMap(5);

        cache.add(1);
        cache.add(2);
        cache.add(3);
        cache.add(4);
        cache.add(5);

        cache.find(1);

        cache.add(6);

        Assert.assertEquals(cache.size(), 5);
        Assert.assertTrue(cache.find(2).isEmpty());
        Assert.assertFalse(cache.find(1).isEmpty());
    }

    @Test
    public void concurrencyTest() throws InterruptedException {
        ForgettingMap cache = new ForgettingMap(5);

        final Callable<String> task = () -> {
            cache.add(1);
            cache.add(2);
            cache.add(3);
            cache.add(4);
            cache.add(5);

            return cache.contents().toString();
        };

        List<Callable<String>> tasks = Arrays.asList(task, task, task);
        ExecutorService service = Executors.newFixedThreadPool(5);
        service.invokeAll(tasks);

        System.out.println(cache.contents());
    }
}
