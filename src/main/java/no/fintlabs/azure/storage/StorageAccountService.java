package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.storage.blob.BlobContainer;
import no.fintlabs.azure.storage.blob.BlobContainerCrd;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class StorageAccountService {

    private final StorageManager storageManager;

    public StorageAccountService(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public StorageAccount add(BlobContainerCrd crd) {

        log.debug("Creating storage account with name: {}", sanitizeStorageAccountName(crd.getMetadata().getName()));
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(sanitizeStorageAccountName(crd.getMetadata().getName()))
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(crd.getSpec().getResourceGroup())
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.HOT)
                .withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .create();

        log.debug("Storage account status: {}", storageAccount.accountStatuses().primary().toString());

        return storageAccount;
    }

    public void delete(BlobContainer blobContainer) {
        log.debug("Removing storage account {}", blobContainer.getStorageAccountName());
        storageManager
                .storageAccounts()
                .deleteByResourceGroup(blobContainer.getResourceGroup(), blobContainer.getStorageAccountName());
    }

    public Optional<StorageAccount> getStorageAccount(BlobContainerCrd primaryResource) {
        log.debug("Fetching Azure blob container for {}...", primaryResource.getMetadata().getName());
        CheckNameAvailabilityResult checkNameAvailabilityResult = storageManager.storageAccounts().checkNameAvailability(sanitizeStorageAccountName(primaryResource.getMetadata().getName()));
        if (checkNameAvailabilityResult.isAvailable()) {
            return Optional.empty();
        }
        return Optional.of(storageManager
                .storageAccounts()
                .getByResourceGroup(primaryResource.getSpec().getResourceGroup(),
                        sanitizeStorageAccountName(primaryResource.getMetadata().getName())
                )
        );
    }

    public String getConnectionString(StorageAccount storageAccount) {
        return String.format(
                "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                storageAccount.name(),
                storageAccount.getKeys().get(0).value()
        );
    }

    public String sanitizeStorageAccountName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
