package no.fintlabs.azure.storage.blob;

import com.azure.resourcemanager.storage.models.*;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.StorageAccountUtilities;
import no.fintlabs.azure.storage.StorageAccountService;
import no.fintlabs.azure.storage.blob.AzureStorageBlobCrd;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BlobContainerService {


private final StorageAccountService storageAccountService;

    public BlobContainerService(StorageAccountService storageAccountService) {
        this.storageAccountService = storageAccountService;
    }


    public AzureBlobContainer add(AzureStorageBlobCrd crd) {

        StorageAccount storageAccount = storageAccountService.add(crd);

        log.info("Creating blob container...");
        BlobContainer container = storageAccount
                .manager()
                .blobContainers().defineContainer(crd.getMetadata().getName())
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .create();

        log.info("Blob container created: {}", container);

        return AzureBlobContainer.builder()
                .blobContainerName(container.name())
                .resourceGroup(storageAccount.resourceGroupName())
                .storageAccountName(storageAccount.name())
                .connectionString(StorageAccountUtilities.getConnectionString(storageAccount))
                .build();

    }

    public void delete(AzureBlobContainer azureBlobContainer) {
        storageAccountService.delete(azureBlobContainer);
    }
}
