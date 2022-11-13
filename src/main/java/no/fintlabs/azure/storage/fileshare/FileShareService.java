package no.fintlabs.azure.storage.fileshare;

import com.azure.resourcemanager.storage.fluent.models.FileShareInner;
import com.azure.resourcemanager.storage.fluent.models.FileShareItemInner;
import com.azure.resourcemanager.storage.models.ProvisioningState;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.StorageAccountService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.fintlabs.MetadataUtils.getStorageAccountName;

@Slf4j
@Service
public class FileShareService {


    private final StorageAccountService storageAccountService;
    private final AzureConfiguration azureConfiguration;

    public FileShareService(StorageAccountService storageAccountService, AzureConfiguration azureConfiguration) {
        this.storageAccountService = storageAccountService;
        this.azureConfiguration = azureConfiguration;
    }


    public FileShare add(FileShare desired, FileShareCrd crd) {

        StorageAccount storageAccount = storageAccountService.add(crd);

        log.debug("Creating file share...");
        FileShareInner fileShare = storageAccount
                .manager()
                .serviceClient()
                .getFileShares()
                .create(azureConfiguration.getStorageAccountResourceGroup(),
                        storageAccount.name(),
                        desired.getShareName(),
                        new FileShareInner()
                );

        log.debug("File share created: {}", fileShare.name());

        desired.setConnectionString(storageAccountService.getConnectionString(storageAccount));
        desired.setStorageAccountName(storageAccount.name());

        return desired;
    }

    public Set<FileShare> get(FileShareCrd crd) {

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
                return Collections.singleton(FileShare.builder()
                        .storageAccountName(storageAccount.name())
                        .resourceGroup(storageAccount.resourceGroupName())
                                .shareName(fileShares.isEmpty() ? "" : fileShares.get(0).name())
                        .connectionString(storageAccountService.getConnectionString(storageAccount))
                        .build());
            } else {
                log.debug("Storage account for {} is not ready yet", crd.getMetadata().getName());
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

    public void delete(FileShare fileShare) {
        storageAccountService.delete(fileShare);
    }
}
