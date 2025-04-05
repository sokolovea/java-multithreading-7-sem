package ru.rsreu.storage;

/**
 * Not Lazy Singleton Storage
 */
public class StorageNotLazy implements StorageSingleton {

    /**
     * Storage that contains fields with the result and progress
     */
    private static final StorageNotLazy storageNotLazy = new StorageNotLazy();

    /**
     * Private storage object
     */
    private final Storage storage = new Storage();

    /**
     * Private constructor for singleton
     */
    private StorageNotLazy() {
        System.out.println("Not Lazy storage has been constructed");
    }

    /**
     * Factory method for getting not lazy storage
     * @return not lazy storage
     */
    public static StorageNotLazy getInstance() {
        return storageNotLazy;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }
}