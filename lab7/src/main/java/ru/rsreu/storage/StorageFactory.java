package ru.rsreu.storage;

/**
 * Factory for constructing storage singletons
 */
public class StorageFactory {
    public static StorageSingleton getStorageSingleton() {
        return StorageInitOnDemand.getInstance();
//        return StorageDoubleCheckedLocking.getInstance();
//        return StorageNotLazy.getInstance();
    }
}
