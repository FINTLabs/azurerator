package no.fintlabs.azure.storage.fileshare;

import com.azure.resourcemanager.storage.fluent.models.FileShareInner;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.StorageAccountService;
import no.fintlabs.azure.storage.StorageResource;
import no.fintlabs.azure.storage.StorageResourceRepository;
import no.fintlabs.azure.storage.StorageType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

import static no.fintlabs.azure.storage.PathFactory.getPathFromStorageAccount;

@Slf4j
@Service
public class FileShareService {


    private final StorageAccountService storageAccountService;
    private final StorageResourceRepository storageResourceRepository;

    public FileShareService(StorageAccountService storageAccountService, StorageResourceRepository storageResourceRepository, AzureConfiguration azureConfiguration) {
        this.storageAccountService = storageAccountService;
        this.storageResourceRepository = storageResourceRepository;
    }


    public StorageResource add(StorageResource desired, FileShareCrd crd) {

        StorageAccount storageAccount = storageAccountService.add(crd, desired.getPath(), StorageType.FILE_SHARE);

        log.debug("Creating file share...");
        FileShareInner fileShare = storageAccount
                .manager()
                .serviceClient()
                .getFileShares()
                .create(
                        storageAccount.resourceGroupName(),
                        storageAccount.name(),
                        desired.getPath(),
                        new FileShareInner()
                );

        log.debug("File share created: {}", fileShare.name());

        return StorageResource.of(storageAccount, desired.getPath(), StorageType.FILE_SHARE);
    }

    public Set<StorageResource> get(FileShareCrd crd) {

        if (storageAccountService.getStorageAccount(crd).isEmpty()) {
            log.info("Storage account for {} is not found", crd.getMetadata().getName());
            return Collections.emptySet();
        }

        StorageAccount storageAccount = storageAccountService.getStorageAccount(crd).get();
        if (storageAccount.provisioningState().equals(ProvisioningState.SUCCEEDED)) {
            log.debug("Storage account for {} is ready", crd.getMetadata().getName());

            StorageResource storageResource =
                    StorageResource.of(storageAccount);

            storageResourceRepository.update(storageResource);
            return Collections.singleton(storageResource);

        } else {
            log.debug("Storage account for {} is not ready yet", crd.getMetadata().getName());
            throw new IllegalStateException("Storage account for " + crd.getMetadata().getName() + " is not ready yet");
        }
    }

    public void delete(StorageResource storageResource) {
        storageAccountService.delete(storageResource);
    }
}
