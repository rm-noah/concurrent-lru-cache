package com.noah;

import java.util.*;
import java.util.concurrent.locks.*;

public class ForgettingMap {

    /**
     * Using a LinkedHashSet remembers the order in which the elements are
     * added to the set and returns the elemnents in that order. Given it's a
     * Hash structure is provides fast lookup.
     */
    private final LinkedHashSet<Integer> set;

    /**
     * Using a HashMap to map the popularity of keys means we can easily find
     * and increment the hits with a complexity of O(n)
     */
    private final HashMap<Integer, Integer> hits;

    private final int capacity;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ForgettingMap(int capacity) {
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

    public Optional<Integer> find(int key) {
        // For concurrent projects we'll lock the read
        // Alternatively we could use the synchronised keyword which would
        // work fine but manually locking/unlocking gives us more control.
        lock.readLock().lock();

        try {
            // Opted for Optionals as the spec doesn't specify a default value to return.
            // "null" could work, but it depends on what the project uses.
            Optional<Integer> record = Optional.empty();

            if (set.contains(key)) {
                if (hits.containsKey(key)) {
                    hits.put(key, hits.get(key) + 1);
                }
                record = Optional.of(key);
            }
            return record;
        } finally {
            // Must always unlock in the `finally` block to prevent deadlock
            lock.readLock().unlock();
        }
    }

    public void add(int association) {
        // Locking the write operation to prevent other threads overwriting
        lock.writeLock().lock();
        try {
            if (set.size() == capacity) {
                // Remove least used key
                Optional<Integer> stale = findLeastFrequent();
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
    public int getPopularity(int key) {
        return hits.getOrDefault(key, 0);
    }

    /**
     * Finds the key with the lowest number of hits. Returns the first occurrence
     * if two or more keys have the same amount of hits.
     *
     * @return Integer to remove, else an empty Optional
     */
    private Optional<Integer> findLeastFrequent() {
        lock.readLock().lock();
        try {
            Optional<Integer> min = Optional.empty();
            if (hits.isEmpty()) {
                return min;
            }
            return set.stream().min(Comparator.comparing(hits::get));
        } finally {
            lock.readLock().unlock();
        }
    }
}
