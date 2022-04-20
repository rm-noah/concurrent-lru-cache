package com.noah;

import java.util.*;

/**
 * Concurrent LRU cache stores a hash set of the given type
 * and removes the least frequently accessed element when the cache
 * is full.
 *
 * @param <T> Type of elements stored in LinkedHashSet
 */
public class ConcurrentLRUCache<T> {

    /**
     * Using a LinkedHashSet remembers the order in which the elements are
     * added to the set and returns the elemnents in that order. Given it's a
     * Hash structure is provides fast lookup.
     */
    private final LinkedHashSet<T> set;

    /**
     * Using a HashMap to map the popularity of keys means we can easily find
     * and increment the hits with a time complexity of O(n)
     */
    private final HashMap<T, Integer> hits;

    /**
     * Maximum size of the LinkedHashSet
     */
    private final int capacity;

    public ConcurrentLRUCache(int capacity) {
        this.capacity = capacity;
        this.set = new LinkedHashSet<>(capacity);
        this.hits = new HashMap<>();
    }

    /**
     * Returns the size of the current LinkedHashSet
     *
     * @return Size of cache
     */
    public synchronized int size() {
        return set.size();
    }

    /**
     * Searches the cache for the given element
     *
     * @param key Desired element
     * @return Optional object containing a key if the key exists in the cache, else an empty Optional
     */
    public synchronized Optional<T> find(T key) {
        // Opted for Optionals as the spec doesn't specify a default value to return.
        // "null" could work, but it depends on what the project uses.
        Optional<T> record = Optional.empty();

        if (set.contains(key)) {
            if (hits.containsKey(key)) {
                hits.put(key, hits.get(key) + 1);
            }
            record = Optional.of(key);
        }
        return record;
    }

    /**
     * Adds an element to the cache
     *
     * @param association Element to add to the cache
     */
    public synchronized void add(T association) {
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
    }

    /**
     * Returns the popularity of a given element, 0 if the element does not exist
     */
    public synchronized int getPopularity(T key) {
        return hits.getOrDefault(key, 0);
    }

    /**
     * Finds the key with the lowest number of hits. Returns the first occurrence
     * if two or more keys have the same amount of hits.
     *
     * @return Integer to remove, else an empty Optional
     */
    private synchronized Optional<T> findLeastFrequent() {
        Optional<T> min = Optional.empty();
        if (hits.isEmpty()) {
            return min;
        }
        return set.stream().min(Comparator.comparing(hits::get));
    }

    public void contents() {
        set.forEach(System.out::println);
    }
}
