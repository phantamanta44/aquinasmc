package xyz.phanta.aquinasmc.util;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Just because {@link java.util.LinkedHashMap} isn't a {@link java.util.NavigableMap} for some stupid reason.
 */
public class InsertionOrderedTable<K, V> {

    private final Map<K, Node> table;
    @Nullable
    private Node head, tail;

    public InsertionOrderedTable(TableFactory<K> factory) {
        this.table = factory.createMap();
    }

    public InsertionOrderedTable() {
        this.table = new HashMap<>();
    }

    public void put(K key, V value) {
        Node node = table.get(key);
        if (node != null) {
            node.value = value;
        } else {
            node = new Node(key, value, tail);
            if (tail == null) {
                head = tail = node;
            } else {
                tail.next = node;
                tail = node;
            }
            table.put(key, node);
        }
    }

    public V get(K key) {
        return Objects.requireNonNull(table.get(key)).value;
    }

    @Nullable
    public K getNext(K key) {
        Node next = Objects.requireNonNull(table.get(key)).next;
        return next != null ? next.key : null;
    }

    @Nullable
    public V getNextValue(K key) {
        Node next = Objects.requireNonNull(table.get(key)).next;
        return next != null ? next.value : null;
    }

    @Nullable
    public K getPrev(K key) {
        Node prev = Objects.requireNonNull(table.get(key)).prev;
        return prev != null ? prev.key : null;
    }

    @Nullable
    public V getPrevValue(K key) {
        Node prev = Objects.requireNonNull(table.get(key)).prev;
        return prev != null ? prev.value : null;
    }

    public K getFirst() {
        return Objects.requireNonNull(head).key;
    }

    public V getFirstValue() {
        return Objects.requireNonNull(head).value;
    }

    public K getLast() {
        return Objects.requireNonNull(tail).key;
    }

    public V getLastValue() {
        return Objects.requireNonNull(tail).value;
    }

    public boolean hasKey(K key) {
        return table.containsKey(key);
    }

    public int size() {
        return table.size();
    }

    @SuppressWarnings("unchecked")
    public List<V> getValues() {
        V[] values = (V[])new Object[table.size()];
        Node node = head;
        for (int i = 0; i < values.length; i++) {
            values[i] = Objects.requireNonNull(node).value;
            node = node.next;
        }
        return Arrays.asList(values);
    }

    private class Node {

        final K key;
        V value;

        @Nullable
        final Node prev;
        @Nullable
        Node next;

        Node(K key, V value, @Nullable Node prev) {
            this.key = key;
            this.value = value;
            this.prev = prev;
        }

    }

    @FunctionalInterface
    public interface TableFactory<K> {

        <V> Map<K, V> createMap();

    }

}
