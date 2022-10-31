package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureSpec;
import no.fintlabs.azure.storage.fileshare.FileShareCrd;
import no.fintlabs.common.FlaisCrd;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class StorageAccountService {

    private final StorageManager storageManager;

    private final Map<String, String> storageAccounts = new HashMap<>();

    public StorageAccountService(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @PostConstruct
    public void init() {
        //storageAccounts.addAll(storageManager.storageAccounts().list().stream().map(HasName::name).collect(Collectors.toSet()));
        storageManager.storageAccounts().list().forEach(storageAccount ->
                storageAccounts.put(
                        getAccountStatusName(storageAccount.resourceGroupName(), storageAccount.name()),
                        storageAccount.accountStatuses().primary().name())
        );
        log.debug("Found {} storage accounts", storageAccounts.size());
    }

    public StorageAccount add(FlaisCrd<? extends AzureSpec> crd) {

        String accountName = sanitizeStorageAccountName(crd.getMetadata().getName());
        log.debug("Creating storage account with name: {}", accountName);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(accountName)
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(crd.getSpec().getResourceGroup())
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                //.withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .create();
        storageAccounts.put(
                getAccountStatusName(storageAccount.resourceGroupName(), storageAccount.name()),
                storageAccount.accountStatuses().primary().name()
        );
        log.debug("Storage account status: {}", storageAccount.accountStatuses().primary().toString());
        log.debug("We got {} storage accounts after adding a new one", storageAccounts.size());


        return storageAccount;
    }

    public void delete(AzureStorageObject blobContainer) {
        log.debug("Removing storage account {}", blobContainer.getStorageAccountName());
        storageManager
                .storageAccounts()
                .deleteByResourceGroup(blobContainer.getResourceGroup(), blobContainer.getStorageAccountName());
        log.debug("We got {} storage accounts before removing", storageAccounts.size());
        storageAccounts.remove(getAccountStatusName(blobContainer.getResourceGroup(), blobContainer.getStorageAccountName()));
        log.debug("Storage account {} removed!", blobContainer.getStorageAccountName());
        log.debug("We got {} storage accounts after removing", storageAccounts.size());
    }

    public Optional<StorageAccount> getStorageAccount(FlaisCrd<? extends AzureSpec> primaryResource) {

        log.debug("Fetching Azure blob container for {}...", primaryResource.getMetadata().getName());

        String sanitizedStorageAccountName = sanitizeStorageAccountName(primaryResource.getMetadata().getName());

        CheckNameAvailabilityResult checkNameAvailabilityResult = storageManager
                .storageAccounts()
                .checkNameAvailability(sanitizedStorageAccountName);
        if (checkNameAvailabilityResult.isAvailable()) {
            return Optional.empty();
        }

        /*
        Since the storage account name is not available we need to check if it is one of our own names.
        See https://learn.microsoft.com/en-us/azure/azure-resource-manager/troubleshooting/error-storage-account-name?tabs=bicep#storage-account-already-taken
        for more information.
         */
        if (storageAccounts.containsKey(getAccountStatusName(primaryResource.getSpec().getResourceGroup(), sanitizedStorageAccountName))) {
            return Optional.of(storageManager
                    .storageAccounts()
                    .getByResourceGroup(primaryResource.getSpec().getResourceGroup(),
                            sanitizeStorageAccountName(primaryResource.getMetadata().getName())
                    )
            );
        }

        throw new IllegalArgumentException("Storage account name " + sanitizedStorageAccountName + " is not globally unique. Storage account names must be globally unique across Azure.");
    }

    public String getStatus(FileShareCrd crd) {
        return storageAccounts.get(getAccountStatusName(crd.getSpec().getResourceGroup(), crd.getMetadata().getName()));
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

    private String getAccountStatusName(String resourceGroup, String name) {
        return String.format("%s/%s",
                resourceGroup,
                sanitizeStorageAccountName(name));
    }
}
