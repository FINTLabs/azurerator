package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.StorageAccountUtilities;
import no.fintlabs.azure.storage.blob.AzureBlobContainer;
import no.fintlabs.azure.storage.blob.AzureStorageBlobCrd;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StorageAccountService {

    @Value("${fint.azure.storage.resource-group-name:rg-storage}")
    private String resourceGroup;
    private final StorageManager storageManager;

    public StorageAccountService(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public StorageAccount add(AzureStorageBlobCrd crd) {

        log.info("Creating storage account with name: {}", StorageAccountUtilities.sanitizeStorageAccountName(crd.getMetadata().getName()));
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(StorageAccountUtilities.sanitizeStorageAccountName(crd.getMetadata().getName()))
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.HOT)
                .withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .create();

        log.info("Storage account status: {}", storageAccount.accountStatuses().primary().toString());

        return storageAccount;
    }

    public void delete(AzureBlobContainer azureBlobContainer) {
        log.info("Removing storage account {}", azureBlobContainer.getStorageAccountName());
        storageManager
                .storageAccounts()
                .deleteByResourceGroup(resourceGroup, azureBlobContainer.getStorageAccountName());
    }
}
