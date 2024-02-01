package no.fintlabs.azure.storage.blob;

import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
public class BlobContainerService {


    private final StorageAccountService storageAccountService;
    private final StorageResourceRepository storageResourceRepository;

    public BlobContainerService(StorageAccountService storageAccountService, StorageResourceRepository storageResourceRepository, AzureConfiguration azureConfiguration) {
        this.storageAccountService = storageAccountService;
        this.storageResourceRepository = storageResourceRepository;
    }


    public StorageResource add(StorageResource desired, BlobContainerCrd crd) {

        StorageAccount storageAccount = storageAccountService.add(crd, desired.getPath(), StorageType.BLOB_CONTAINER);

        log.debug("Creating blob container...");
        BlobContainer container = storageAccount
                .manager()
                .blobContainers()
                .defineContainer(desired.getPath())
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .create();

        log.debug("Blob container created: {}", container.name());

        return StorageResource.of(storageAccount, desired.getPath(), StorageType.BLOB_CONTAINER);
    }

    public Set<StorageResource> get(BlobContainerCrd crd) {

        if (storageAccountService.getStorageAccount(crd).isEmpty()) {
            // TODO: FLA-226: Possible problem here if we return emptySet()
            log.info("Storage account for {} is not found", crd.getMetadata().getName());
            return Collections.emptySet();
        }

        StorageAccount storageAccount = storageAccountService.getStorageAccount(crd).get();
        if (storageAccount.provisioningState().equals(ProvisioningState.SUCCEEDED)) {
            log.debug("Storage account for {} is ready", crd.getMetadata().getName());

            StorageResource storageResource = StorageResource.of(
                    storageAccount,
                    PathFactory.getPathFromStorageAccount(storageAccount, StorageType.BLOB_CONTAINER),
                    StorageType.BLOB_CONTAINER);
            storageResourceRepository.update(storageResource);

            return Collections.singleton(storageResource);

        } else {
            log.warn("Storage account for {} is not ready yet", crd.getMetadata().getName());
            throw new IllegalStateException("Storage account for " + crd.getMetadata().getName() + " is not ready yet");
        }
    }

    public void delete(StorageResource blobContainer) {
        storageAccountService.delete(blobContainer);
    }
}
