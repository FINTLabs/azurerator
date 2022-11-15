package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisCrd;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.AzureSpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static no.fintlabs.MetadataUtils.*;
import static no.fintlabs.azure.TagNames.TAG_ORG_ID;
import static no.fintlabs.azure.TagNames.TAG_TEAM;

@Slf4j
@Service
public class StorageAccountService {


    private final StorageManager storageManager;

    private final StorageAccountRepository storageAccountRepository;

    private final AzureConfiguration azureConfiguration;

    public StorageAccountService(StorageManager storageManager, StorageAccountRepository storageAccountRepository, AzureConfiguration azureConfiguration) {
        this.storageManager = storageManager;
        this.storageAccountRepository = storageAccountRepository;
        this.azureConfiguration = azureConfiguration;
    }

    public StorageAccount add(FlaisCrd<? extends AzureSpec> crd) {

        String accountName = generateStorageAccountName();
        log.debug("Creating storage account with name: {}", accountName);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(accountName)
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(azureConfiguration.getStorageAccountResourceGroup())
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .withTag(TAG_ORG_ID, getOrgId(crd).orElse("N/A"))
                .withTag(TAG_TEAM, getTeam(crd).orElse("N/A"))
                .create();

        storageAccountRepository.add(AzureStorageObject.of(storageAccount));

        crd.getMetadata().getAnnotations().put(ANNOTATION_STORAGE_ACCOUNT_NAME, accountName);
        log.debug("Storage account status: {}", storageAccount.accountStatuses().primary().toString());
        log.debug("We got {} storage accounts after adding a new one", storageAccountRepository.size());

        return storageAccount;
    }

    public void delete(AzureStorageObject azureStorageObject) {
        log.debug("Removing storage account {}", azureStorageObject.getStorageAccountName());
        storageManager
                .storageAccounts()
                .deleteByResourceGroup(azureStorageObject.getResourceGroup(), azureStorageObject.getStorageAccountName());
        log.debug("We got {} storage accounts before removing", storageAccountRepository.size());
        storageAccountRepository.remove(azureStorageObject);
        log.debug("Storage account {} removed!", azureStorageObject.getStorageAccountName());
        log.debug("We got {} storage accounts after removing", storageAccountRepository.size());
    }

    public Optional<StorageAccount> getStorageAccount(FlaisCrd<? extends AzureSpec> primaryResource) {

        log.debug("Check if Azure Storage Account for resource {} exists", primaryResource.getMetadata().getName());

        Optional<String> storageAccountName = getStorageAccountName(primaryResource);

        if (storageAccountName.isEmpty()) {
            return Optional.empty();
        }

        if (storageAccountRepository.exists(storageAccountName.get())) {
            log.debug("Fetching Azure Storage Account {} ...", getStorageAccountName(primaryResource).orElse("N/A"));
            return Optional.of(storageManager
                    .storageAccounts()
                    .getByResourceGroup(azureConfiguration.getStorageAccountResourceGroup(),
                            storageAccountName.get())
            );
        }

        return Optional.empty();

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
}
