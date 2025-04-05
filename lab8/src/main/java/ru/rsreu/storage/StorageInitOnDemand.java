package ru.rsreu.storage;


/**
 * Storage Init On Demand with lazy storage
 */
public class StorageInitOnDemand implements StorageSingleton {

    /**
     * Storage contains fields with the progress
     */
    private final Storage storage;

    /**
     * Constructs once
     */
    private StorageInitOnDemand() {
        System.out.println("Lazy storage was created!");
        this.storage = new Storage();
    }

    /**
     * Internal class for Init On Demand support
     */
    private static class LazyStorage {
        static final StorageInitOnDemand INSTANCE = new StorageInitOnDemand();
    }

    /**
     * Factory method for storage init on demand
     *
     * @return lazy storage with double init on demand
     */
    public static StorageInitOnDemand getInstance() {
        return LazyStorage.INSTANCE;
    }

    @Override
    public synchronized Storage getStorage() {
        return storage;
    }

}
