package Store;

public class DataStore {

    private static class Entry {
        final String value;
        final Long expiryAt;

        Entry(String value, Long expiryAt) {
            this.value = value;
            this.expiryAt = expiryAt;
        }

        boolean isExpired() {
            return expiryAt != null && System.currentTimeMillis() > expiryAt;
        }
    }

    private final LruCache<String, Entry> store;

    public DataStore(int capacity) {
        this.store = new LruCache<>(capacity);
    }

    public void set(String key, String value) {
        store.put(key, new Entry(value, null));
    }

    public String get(String key) {
        Entry entry = store.computeIfPresent(key, (k, e) -> e.isExpired() ? null : e);
        return entry != null ? entry.value : null;
    }

    public boolean del(String key) {
        return store.remove(key) != null;
    }

    public boolean exists(String key) {
        return get(key) != null;
    }

    public boolean expire(String key, long seconds) {
        Entry updated = store.computeIfPresent(key,
                (k, e) -> new Entry(e.value, System.currentTimeMillis() + seconds * 1000));
        return updated != null;
    }
}