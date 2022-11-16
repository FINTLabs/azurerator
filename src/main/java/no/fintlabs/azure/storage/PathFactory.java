package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.fluent.models.FileShareItemInner;
import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

public class PathFactory {
    public static String getPathFromStorageAccount(StorageAccount storageAccount, StorageType storageType) {

        switch (storageType) {
            case FILE_SHARE:
                List<FileShareItemInner> fileShares = storageAccount
                        .manager()
                        .serviceClient()
                        .getFileShares()
                        .list(storageAccount.resourceGroupName(), storageAccount.name())
                        .stream()
                        .toList();
                return fileShares.isEmpty() ? "" : fileShares.get(0).name();
            case BLOB_CONTAINER:
                List<ListContainerItemInner> blobContainers = storageAccount
                        .manager()
                        .blobContainers()
                        .list(storageAccount.resourceGroupName(), storageAccount.name())
                        .stream().toList();
                return blobContainers.isEmpty() ? "" : blobContainers.get(0).name();
            default:
                return StorageType.UNKNOWN.name();

        }
    }

    static String generatePathName() {
        return RandomStringUtils.randomAlphabetic(12).toLowerCase();
    }
}
