package com.noah;

import java.util.*;
import java.util.concurrent.locks.*;

public class ForgettingMap {

    /**
     * Using a set allows us to maintain the order of insertion
     * that a list or array wouldn't. HashSet/HashMap would maintain
     * order of insertion but we'd be disallowing duplicates which isn't
     * specified in the spec.
     */
    Set<Integer> cache;
    int capacity;

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ForgettingMap(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashSet<>(capacity);
    }

    public void add(int association) {
        lock.writeLock().lock();
        try {
            if (cache.size() == capacity) {
                // Remove least used key
                cache.remove(cache.iterator().next());
            }
            cache.add(association);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Integer> find(Integer key) {
        // For concurrent projects we'll lock the read
        // Alternatively we could use the synchronised keyword which would
        // work fine but manually locking/unlocking gives us more control.
        lock.readLock().lock();

        try {
            // Opted for Optionals as the spec doesn't specify a default value to return.
            // "null" could work, but it depends on what the project uses.
            Optional<Integer> record = Optional.empty();

            if (cache.contains(key)) {
                // Helps us keep the most recently used key at the "top" of the set
                cache.remove(key);
                cache.add(key);
                record = Optional.of(key);
            }
            return record;
        } finally {
            // Must always unlock in the `finally` block to prevent deadlock
            lock.readLock().unlock();
        }
    }
}
