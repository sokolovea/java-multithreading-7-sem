package ru.rsreu.storage;

/**
 * Interface for storage singleton wrappers
 */
public interface StorageSingleton {

    /**
     * Gets storage object from wrapper
     *
     * @return storage object from wrapper
     */
    public Storage getStorage();
}
