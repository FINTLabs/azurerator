package no.fintlabs.azure.storage.blob;

import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.storage.StorageAccountService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class BlobContainerService {


    private final StorageAccountService storageAccountService;

    public BlobContainerService(StorageAccountService storageAccountService) {
        this.storageAccountService = storageAccountService;
    }


    public BlobContainer add(BlobContainerCrd crd) {

        StorageAccount storageAccount = storageAccountService.add(crd);

        log.debug("Creating blob container...");
        com.azure.resourcemanager.storage.models.BlobContainer container = storageAccount
                .manager()
                .blobContainers()
                .defineContainer(crd.getMetadata().getName())
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .create();

        log.debug("Blob container created: {}", container);

        return BlobContainer.builder()
                .blobContainerName(container.name())
                .resourceGroup(storageAccount.resourceGroupName())
                .storageAccountName(storageAccount.name())
                .connectionString(storageAccountService.getConnectionString(storageAccount))
                .build();

    }

    public Set<BlobContainer> get(BlobContainerCrd crd) {


        if (storageAccountService.getStorageAccount(crd).isPresent()) {

            StorageAccount storageAccount = storageAccountService.getStorageAccount(crd).get();
            if (storageAccount.provisioningState().equals(ProvisioningState.SUCCEEDED)) {
                log.debug("Storage account for {} is ready", crd.getMetadata().getName());
                List<ListContainerItemInner> list = storageAccount
                        .manager()
                        .blobContainers()
                        .list(crd.getSpec().getResourceGroup(), storageAccountService.getStorageAccountNameFromAnnotation(crd)
                                .orElseThrow(() -> new IllegalArgumentException("Unable to get storage account name from annotation")))
                        .stream().toList();

                return Collections.singleton(BlobContainer.builder()
                        .storageAccountName(storageAccount.name())
                        .resourceGroup(storageAccount.resourceGroupName())
                        .blobContainerName(list.isEmpty() ? "" : list.get(0).name())
                        .connectionString(storageAccountService.getConnectionString(storageAccount))
                        .build());
            } else {
                log.debug("Storage account for {} is not ready yet", crd.getMetadata().getName());
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();

    }

    public void delete(BlobContainer blobContainer) {
        storageAccountService.delete(blobContainer);
    }
}
