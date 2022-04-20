package com.noah;

import java.util.*;
import java.util.concurrent.locks.*;

public class ConcurrentLRUCache<T> {

    /**
     * Using a LinkedHashSet remembers the order in which the elements are
     * added to the set and returns the elemnents in that order. Given it's a
     * Hash structure is provides fast lookup.
     */
    private final LinkedHashSet<T> set;

    /**
     * Using a HashMap to map the popularity of keys means we can easily find
     * and increment the hits with a complexity of O(n)
     */
    private final HashMap<T, Integer> hits;

    private final int capacity;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ConcurrentLRUCache(int capacity) {
        this.capacity = capacity;
        this.set = new LinkedHashSet<>(capacity);
        this.hits = new HashMap<>();
    }

    public int size() {
        lock.readLock().lock();
        try {
            return set.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<T> find(T key) {
        // For concurrent projects we'll lock the read
        // Alternatively we could use the synchronised keyword which would
        // work fine but manually locking/unlocking gives us more control.
        lock.readLock().lock();

        try {
            // Opted for Optionals as the spec doesn't specify a default value to return.
            // "null" could work, but it depends on what the project uses.
            Optional<T> record = Optional.empty();

            if (set.contains(key)) {
                if (hits.containsKey(key)) {
                    lock.writeLock().lock();
                    hits.put(key, hits.get(key) + 1);
                }
                record = Optional.of(key);
            }
            return record;
        } finally {
            // Must always unlock in the `finally` block to prevent deadlock
            lock.readLock().unlock();
            lock.writeLock().unlock();
        }
    }

    public void add(T association) {
        // Locking the write operation to prevent other threads overwriting
        lock.writeLock().lock();
        try {
            if (set.size() == capacity) {
                // Remove least used key
                Optional<T> stale = findLeastFrequent();
                if (stale.isPresent()) {
                    set.remove(stale.get());
                    hits.remove(stale.get());
                }
            }
            set.add(association);
            hits.put(association, 1);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Helper method for testing
     */
    public int getPopularity(T key) {
        return hits.getOrDefault(key, 0);
    }

    /**
     * Finds the key with the lowest number of hits. Returns the first occurrence
     * if two or more keys have the same amount of hits.
     *
     * @return Integer to remove, else an empty Optional
     */
    private Optional<T> findLeastFrequent() {
        lock.readLock().lock();
        try {
            Optional<T> min = Optional.empty();
            if (hits.isEmpty()) {
                return min;
            }
            return set.stream().min(Comparator.comparing(hits::get));
        } finally {
            lock.readLock().unlock();
        }
    }
}
