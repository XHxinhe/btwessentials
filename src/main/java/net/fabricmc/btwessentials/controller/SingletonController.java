package net.fabricmc.btwessentials.controller;

public final class SingletonController {

    private static BackupController backupControllerInstance;
    private static ConfigurationController configurationControllerInstance;
    private static CleanupController cleanupControllerInstance;
    private static InMemoryController inMemoryControllerInstance;
    private static WorldLoaderController worldLoaderControllerInstance;

    public static BackupController getBackupController() {
        if (backupControllerInstance == null) {
            backupControllerInstance = new BackupController();
        }

        return backupControllerInstance;
    }

    public static ConfigurationController getConfigurationController() {
        if (configurationControllerInstance == null) {
            configurationControllerInstance = new ConfigurationController();
        }

        return configurationControllerInstance;
    }

    public static CleanupController getCleanupController() {
        if (cleanupControllerInstance == null) {
            cleanupControllerInstance = new CleanupController();
        }

        return cleanupControllerInstance;
    }

    public static InMemoryController getInMemoryControllerInstance() {
        if (inMemoryControllerInstance == null) {
            inMemoryControllerInstance = new InMemoryController();
        }

        return inMemoryControllerInstance;
    }

    public static WorldLoaderController getWorldLoaderControllerInstance() {
        if (worldLoaderControllerInstance == null) {
            worldLoaderControllerInstance = new WorldLoaderController();
        }

        return worldLoaderControllerInstance;
    }
}
