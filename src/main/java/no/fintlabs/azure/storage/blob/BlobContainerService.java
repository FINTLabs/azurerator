package no.fintlabs.azure.storage.blob;

import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.StorageAccountService;
import no.fintlabs.azure.storage.StorageResource;
import no.fintlabs.azure.storage.StorageResourceRepository;
import no.fintlabs.azure.storage.StorageType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.fintlabs.MetadataUtils.getStorageAccountName;

@Slf4j
@Service
public class BlobContainerService {


    private final StorageAccountService storageAccountService;
    private final StorageResourceRepository storageResourceRepository;
    private final AzureConfiguration azureConfiguration;

    public BlobContainerService(StorageAccountService storageAccountService, StorageResourceRepository storageResourceRepository, AzureConfiguration azureConfiguration) {
        this.storageAccountService = storageAccountService;
        this.storageResourceRepository = storageResourceRepository;
        this.azureConfiguration = azureConfiguration;
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


        if (storageAccountService.getStorageAccount(crd).isPresent()) {

            StorageAccount storageAccount = storageAccountService.getStorageAccount(crd).get();
            if (storageAccount.provisioningState().equals(ProvisioningState.SUCCEEDED)) {
                log.debug("Storage account for {} is ready", crd.getMetadata().getName());
                List<ListContainerItemInner> blobContainers = storageAccount
                        .manager()
                        .blobContainers()
                        .list(azureConfiguration.getStorageAccountResourceGroup(), getStorageAccountName(crd)
                                .orElseThrow(() -> new IllegalArgumentException("Unable to get storage account name from annotation")))
                        .stream().toList();

                StorageResource storageResource = StorageResource.of(storageAccount, blobContainers.isEmpty() ? "" : blobContainers.get(0).name(), StorageType.BLOB_CONTAINER);
                storageResourceRepository.update(storageResource);

                return Collections.singleton(storageResource);

            } else {
                log.debug("Storage account for {} is not ready yet", crd.getMetadata().getName());
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();

    }

    public void delete(StorageResource blobContainer) {
        storageAccountService.delete(blobContainer);
    }
}
