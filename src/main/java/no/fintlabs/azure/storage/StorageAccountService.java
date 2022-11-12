package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisCrd;
import no.fintlabs.azure.AzureSpec;
import no.fintlabs.azure.Defaults;
import no.fintlabs.azure.storage.fileshare.FileShareCrd;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static no.fintlabs.azure.Defaults.ANNOTATION_STORAGE_ACCOUNT_NAME;

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
        storageManager.storageAccounts().list()
                .stream()
                .filter(storageAccount -> storageAccount.resourceGroupName().equals(Defaults.RESOURCE_GROUP))
                .forEach(storageAccount ->
                storageAccounts.put(
                        getAccountStatusName(storageAccount.resourceGroupName(), storageAccount.name()),
                        storageAccount.accountStatuses().primary().name())
        );
        log.debug("Found {} storage accounts", storageAccounts.size());
    }

    public StorageAccount add(FlaisCrd<? extends AzureSpec> crd) {

        String accountName = generateStorageAccountName();
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
        crd.getMetadata().getAnnotations().put(ANNOTATION_STORAGE_ACCOUNT_NAME, accountName);
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

        log.debug("Check if Azure Storage Account for resource {} exists", primaryResource.getMetadata().getName());

        Optional<String> storageAccountName = getStorageAccountNameFromAnnotation(primaryResource);

        if (storageAccountName.isEmpty()) {
            return Optional.empty();
        }

        if (storageAccounts.containsKey(getAccountStatusName(primaryResource.getSpec().getResourceGroup(), storageAccountName.get()))) {
            log.debug("Fetching Azure blob container for {}...", getStorageAccountNameFromAnnotation(primaryResource));
            return Optional.of(storageManager
                    .storageAccounts()
                    .getByResourceGroup(primaryResource.getSpec().getResourceGroup(),
                            storageAccountName.get())
                    );
        }

        return Optional.empty();

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

    public String generateStorageAccountName() {
        String name = "azurerator" + RandomStringUtils.randomAlphanumeric(14).toLowerCase();

        CheckNameAvailabilityResult checkNameAvailabilityResult = storageManager
                .storageAccounts()
                .checkNameAvailability(name);

        while (!checkNameAvailabilityResult.isAvailable()) {
            name = "azurerator" + RandomStringUtils.randomAlphanumeric(14).toLowerCase();

            checkNameAvailabilityResult = storageManager
                    .storageAccounts()
                    .checkNameAvailability(name);
        }

        return name;
    }

    public Optional<String> getStorageAccountNameFromAnnotation(FlaisCrd<? extends AzureSpec> primaryResource) {
        return Optional.ofNullable(primaryResource.getMetadata().getAnnotations().get(ANNOTATION_STORAGE_ACCOUNT_NAME));
    }

    private String getAccountStatusName(String resourceGroup, String name) {
        return String.format("%s/%s",
                resourceGroup,
                name);
    }
}
