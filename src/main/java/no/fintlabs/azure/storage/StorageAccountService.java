package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureCrd;
import no.fintlabs.azure.AzureSpec;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StorageAccountService {

    private final StorageManager storageManager;

    private final Set<String> storageAccounts = new HashSet<>();

    public StorageAccountService(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @PostConstruct
    public void init() {
        storageAccounts.addAll(storageManager.storageAccounts().list().stream().map(HasName::name).collect(Collectors.toSet()));
        log.debug("Found {} storage accounts", storageAccounts.size());
    }

    public StorageAccount add(AzureCrd<? extends AzureSpec, ?> crd) {

        log.debug("Creating storage account with name: {}", sanitizeStorageAccountName(crd.getMetadata().getName()));
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(sanitizeStorageAccountName(crd.getMetadata().getName()))
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(crd.getSpec().getResourceGroup())
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                //.withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .create();

        log.debug("Storage account status: {}", storageAccount.accountStatuses().primary().toString());

        return storageAccount;
    }

    public void delete(AzureStorageObject blobContainer) {
        log.debug("Removing storage account {}", blobContainer.getStorageAccountName());
        storageManager
                .storageAccounts()
                .deleteByResourceGroup(blobContainer.getResourceGroup(), blobContainer.getStorageAccountName());
        log.debug("Storage account {} removed!", blobContainer.getStorageAccountName());
    }

    public Optional<StorageAccount> getStorageAccount(AzureCrd<? extends AzureSpec, ?> primaryResource) {

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
        if (storageAccounts.contains(sanitizedStorageAccountName)) {
            return Optional.of(storageManager
                    .storageAccounts()
                    .getByResourceGroup(primaryResource.getSpec().getResourceGroup(),
                            sanitizeStorageAccountName(primaryResource.getMetadata().getName())
                    )
            );
        }

        throw new IllegalArgumentException("Storage account name " + sanitizedStorageAccountName + " is not globally unique. Storage account names must be globally unique across Azure.");
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
}
