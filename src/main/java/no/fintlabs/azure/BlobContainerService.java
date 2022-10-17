package no.fintlabs.azure;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.*;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.AzureStorageBlobCrd;
import no.fintlabs.AzureStorageBlobStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class BlobContainerService {

    @Value("${fint.azure.storage.resource-group-name:rg-storage}")
    private String resourceGroup;
    private final StorageManager storageManager;

    public BlobContainerService(StorageManager storageManager) {
        this.storageManager = storageManager;
    }


    public StorageAccount add(AzureStorageBlobCrd crd) {


        log.info("Creating storage account {}", StorageAccountUtilities.sanitizeStorageAccountName(crd.getMetadata().getName()));
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

        log.info("Creating blob container...");
        BlobContainer container = storageAccount
                .manager()
                .blobContainers().defineContainer(RandomStringUtils.randomAlphabetic(16))
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .create();

        log.info("Blob container created");

        return storageAccount;

    }
}
