package no.fintlabs.azure.storage.blob;

import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.AzureStorageObject;
import no.fintlabs.azure.storage.StorageAccountRepository;
import no.fintlabs.azure.storage.StorageAccountService;
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
    private final StorageAccountRepository storageAccountRepository;
    private final AzureConfiguration azureConfiguration;

    public BlobContainerService(StorageAccountService storageAccountService, StorageAccountRepository storageAccountRepository, AzureConfiguration azureConfiguration) {
        this.storageAccountService = storageAccountService;
        this.storageAccountRepository = storageAccountRepository;
        this.azureConfiguration = azureConfiguration;
    }


    public AzureStorageObject add(AzureStorageObject desired, BlobContainerCrd crd) {

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

        return AzureStorageObject.of(storageAccount, desired.getPath(), StorageType.BLOB_CONTAINER);
    }

    public Set<AzureStorageObject> get(BlobContainerCrd crd) {


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

                AzureStorageObject azureStorageObject = AzureStorageObject.of(storageAccount, blobContainers.isEmpty() ? "" : blobContainers.get(0).name(), StorageType.BLOB_CONTAINER);
                storageAccountRepository.update(azureStorageObject);

                return Collections.singleton(azureStorageObject);
//                        Collections.singleton(BlobContainer.builder()
//                        .storageAccountName(storageAccount.name())
//                        .resourceGroup(storageAccount.resourceGroupName())
//                        .blobContainerName(blobContainers.isEmpty() ? "" : blobContainers.get(0).name())
//                        .connectionString(storageAccountService.getConnectionString(storageAccount))
//                        .build());
            } else {
                log.debug("Storage account for {} is not ready yet", crd.getMetadata().getName());
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();

    }

    public void delete(AzureStorageObject blobContainer) {
        storageAccountService.delete(blobContainer);
    }
}
