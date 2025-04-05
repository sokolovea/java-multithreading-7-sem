package ru.rsreu.storage;

/**
 * Lazy storage with double-checked locking
 */
public class StorageDoubleCheckedLocking implements StorageSingleton {

    /**
     * Contains fields with the result and progress
     */
    private static Storage storage = null;
    /**
     * Private constructor for singleton
     */
    private StorageDoubleCheckedLocking() {
    }

    /**
     * Factory method for double-checked locking
     *
     * @return lazy storage with double init on demand
     */
    public static StorageSingleton getInstance() {
        return new StorageDoubleCheckedLocking();
    }

    @Override
    public Storage getStorage() {
        if (storage == null) {
            synchronized (StorageDoubleCheckedLocking.class) {
                if (storage == null) {
                    storage = new Storage();
                    System.out.println("Double checked locking storage was created!");
                }
            }
        }
        return storage;
    }
}
