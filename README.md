# Concurrent LRU Cache
<hr>

## What is it?

Custom implementation of the popular LRU Cache pattern which is a structure that removes the least recently used element when the cache is
full.

## How is it different?

This implementation extends that pattern by introducing a multi-threaded component to it.

This implementation also introduces an extension of the "Least-Reused" part of the pattern. Instead of mimicking a Stack structure, this
structure will track the number of times an element is accessed. Instead of removing the element at the bottom of the list this pattern will
remove the least frequently-accessed element. The more an element is accessed, the lower priority it is when being removed.

## Project Structure

Project is built using Maven to provide the JUnit dependency.