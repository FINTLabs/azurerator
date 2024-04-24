package no.fintlabs.azure.storage;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisCrd;
import no.fintlabs.Props;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.AzureSpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static no.fintlabs.MetadataUtils.*;
import static no.fintlabs.azure.TagNames.*;

@Slf4j
@Service
public class StorageAccountService {


    private final StorageManager storageManager;

    private final StorageResourceRepository storageResourceRepository;

    private final AzureConfiguration azureConfiguration;

    public StorageAccountService(StorageManager storageManager, StorageResourceRepository storageResourceRepository, AzureConfiguration azureConfiguration) {
        this.storageManager = storageManager;
        this.storageResourceRepository = storageResourceRepository;
        this.azureConfiguration = azureConfiguration;
    }

    public StorageAccount add(FlaisCrd<? extends AzureSpec> crd) {
        return add(crd, null, StorageType.UNKNOWN);
    }

    public StorageAccount add(FlaisCrd<? extends AzureSpec> crd, String path, StorageType type) {

        String accountName = generateStorageAccountName();
        log.debug("Creating storage account with name: {}", accountName);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(accountName)
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(azureConfiguration.getStorageAccountResourceGroup())
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                //.withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .withTag(TAG_ORG_ID, getOrgId(crd).orElse("N/A"))
                .withTag(TAG_TEAM, getTeam(crd).orElse("N/A"))
                .withTag(TAG_TYPE, type.name())
                .withTag(TAG_INSTANCE, crd.getMetadata().getLabels().get("app.kubernetes.io/instance"))
                .withTag(TAG_PART_OF, crd.getMetadata().getLabels().get("app.kubernetes.io/part-of"))
                .withTag(TAG_CRD_NAME, crd.getMetadata().getName())
                .withTag(TAG_CRD_NAMESPACE, crd.getMetadata().getNamespace())
                .withTag(TAG_ENVIRONMENT, Props.getEnvironment())
                .create();

        if (!storageAccount.name().equals(accountName)) {
            throw new IllegalStateException("Name of StorageAccount doesn't match accountName (storageAccount: " + storageAccount.name() + ", accountName: " + accountName + ")");
        }

        storageResourceRepository.add(StorageResource.of(storageAccount, path, type));

        if (!storageResourceRepository.exists(accountName)) {
            throw new IllegalStateException("Account name:" + accountName + " does not exist in storageResourceRepository");
        }

        crd.getMetadata().getAnnotations().put(ANNOTATION_STORAGE_ACCOUNT_NAME, accountName);

        if (!getStorageAccountName(crd).orElseThrow().equals(accountName)) {
            throw new IllegalStateException("CRD name: " + getStorageAccountName(crd) +" is not matching accountName: " + accountName);
        }

        log.debug("Storage account status: {}", storageAccount.accountStatuses().primary().toString());
        log.debug("We got {} storage accounts in {} after adding a new one", storageResourceRepository.size(Props.getEnvironment()), Props.getEnvironment());

        return storageAccount;
    }

    public void delete(StorageResource storageResource) {
        log.debug("Removing storage account {}", storageResource.getStorageAccountName());
        storageManager
                .storageAccounts()
                .deleteByResourceGroup(storageResource.getResourceGroup(), storageResource.getStorageAccountName());
        log.debug("We got {} storage accounts in {} before removing", storageResourceRepository.size(Props.getEnvironment()), Props.getEnvironment());
        storageResourceRepository.remove(storageResource);
        log.debug("Storage account {} removed!", storageResource.getStorageAccountName());
        log.debug("We got {} storage accounts in {} after removing", storageResourceRepository.size(Props.getEnvironment()), Props.getEnvironment());
    }

    public Optional<StorageAccount> getStorageAccount(FlaisCrd<? extends AzureSpec> primaryResource) {

        log.debug("Check if Azure Storage Account for resource {} exists", primaryResource.getMetadata().getName());

        log.debug("Dump value of primaryResource {}", primaryResource);

        Optional<String> storageAccountName = getStorageAccountName(primaryResource);

        log.debug("Dump value of storageAccountName {}", storageAccountName);

        if (storageAccountName.isEmpty()) {
            return Optional.empty();
        }

        if (storageResourceRepository.exists(storageAccountName.get())) {
            log.debug("Fetching Azure Storage Account {} ...", getStorageAccountName(primaryResource).orElse("N/A"));
            return Optional.of(storageManager
                    .storageAccounts()
                    .getByResourceGroup(azureConfiguration.getStorageAccountResourceGroup(),
                            storageAccountName.get())
            );
        }

        return Optional.empty();

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
