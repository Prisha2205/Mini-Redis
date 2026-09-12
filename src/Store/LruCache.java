package Store;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class LruCache<K, V> {

    private class Node {
        K key;
        V value;
        Node prev, next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final HashMap<K, Node> map;
    private final Node head; // dummy head — head.next is most recently used
    private final Node tail; // dummy tail — tail.prev is least recently used

    public LruCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node(null, null);
        this.tail = new Node(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public synchronized V get(K key) {
        Node node = map.get(key);
        if (node == null) return null;
        moveToFront(node);
        return node.value;
    }

    public synchronized void put(K key, V value) {
        Node existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }

        Node node = new Node(key, value);
        map.put(key, node);
        addToFront(node);

        if (map.size() > capacity) {
            Node lru = tail.prev;
            removeNode(lru);
            map.remove(lru.key);
        }
    }

    public synchronized V remove(K key) {
        Node node = map.remove(key);
        if (node == null) return null;
        removeNode(node);
        return node.value;
    }

    public synchronized boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Atomic read-modify-write, mirroring ConcurrentHashMap.computeIfPresent.
     * Needed because put()/get() alone can't express "read, decide, then
     * write-or-delete" as one step without a caller-visible gap.
     */
    public synchronized V computeIfPresent(K key, BiFunction<K, V, V> remappingFn) {
        Node node = map.get(key);
        if (node == null) return null;

        V updated = remappingFn.apply(key, node.value);
        if (updated == null) {
            removeNode(node);
            map.remove(key);
            return null;
        }

        node.value = updated;
        moveToFront(node);
        return updated;
    }

    public synchronized int size() {
        return map.size();
    }

    // --- internal doubly linked list helpers ---

    private void addToFront(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToFront(Node node) {
        removeNode(node);
        addToFront(node);
    }
}