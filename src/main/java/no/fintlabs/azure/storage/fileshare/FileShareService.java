package no.fintlabs.azure.storage.fileshare;

import com.azure.resourcemanager.storage.fluent.models.FileShareInner;
import com.azure.resourcemanager.storage.fluent.models.FileShareItemInner;
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
import java.util.List;
import java.util.Set;

import static no.fintlabs.MetadataUtils.getStorageAccountName;

@Slf4j
@Service
public class FileShareService {


    private final StorageAccountService storageAccountService;
    private final StorageResourceRepository storageResourceRepository;
    private final AzureConfiguration azureConfiguration;

    public FileShareService(StorageAccountService storageAccountService, StorageResourceRepository storageResourceRepository, AzureConfiguration azureConfiguration) {
        this.storageAccountService = storageAccountService;
        this.storageResourceRepository = storageResourceRepository;
        this.azureConfiguration = azureConfiguration;
    }


    public StorageResource add(StorageResource desired, FileShareCrd crd) {

        StorageAccount storageAccount = storageAccountService.add(crd, desired.getPath(), StorageType.FILE_SHARE);

        log.debug("Creating file share...");
        FileShareInner fileShare = storageAccount
                .manager()
                .serviceClient()
                .getFileShares()
                .create(azureConfiguration.getStorageAccountResourceGroup(),
                        storageAccount.name(),
                        desired.getPath(),
                        new FileShareInner()
                );

        log.debug("File share created: {}", fileShare.name());

        return StorageResource.of(storageAccount, desired.getPath(), StorageType.FILE_SHARE);
    }

    public Set<StorageResource> get(FileShareCrd crd) {

        if (storageAccountService.getStorageAccount(crd).isPresent()) {

            StorageAccount storageAccount = storageAccountService.getStorageAccount(crd).get();
            if (storageAccount.provisioningState().equals(ProvisioningState.SUCCEEDED)) {
                log.debug("Storage account for {} is ready", crd.getMetadata().getName());

                List<FileShareItemInner> fileShares = storageAccount
                        .manager()
                        .serviceClient()
                        .getFileShares()
                        .list(azureConfiguration.getStorageAccountResourceGroup(), getStorageAccountName(crd)
                                .orElseThrow(() -> new IllegalArgumentException("Unable to get storage account name from annotation")))
                        .stream()
                        .toList();

                StorageResource storageResource =
                        StorageResource.of(
                                storageAccount, fileShares.isEmpty() ? "" : fileShares.get(0).name(),
                                StorageType.FILE_SHARE
                        );

                storageResourceRepository.update(storageResource);

                return Collections.singleton(storageResource);

            } else {
                log.debug("Storage account for {} is not ready yet", crd.getMetadata().getName());
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

    public void delete(StorageResource storageResource) {
        storageAccountService.delete(storageResource);
    }
}
