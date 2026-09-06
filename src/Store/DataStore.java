package Store;

import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiryTimes = new ConcurrentHashMap<>();

    public void set(String key, String value) {
        store.put(key, value);
        expiryTimes.remove(key); // overwriting a key clears any old TTL
    }

    public String get(String key) {
        if (isExpired(key)) {
            store.remove(key);
            expiryTimes.remove(key);
            return null;
        }
        return store.get(key);
    }

    public boolean del(String key) {
        expiryTimes.remove(key);
        return store.remove(key) != null;
    }

    public boolean exists(String key) {
        return get(key) != null;
    }

    public boolean expire(String key, long seconds) {
        if (!store.containsKey(key)) return false;
        expiryTimes.put(key, System.currentTimeMillis() + seconds * 1000);
        return true;
    }

    private boolean isExpired(String key) {
        Long expiry = expiryTimes.get(key);
        return expiry != null && System.currentTimeMillis() > expiry;
    }
}