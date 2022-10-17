package no.fintlabs;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import com.azure.resourcemanager.storage.models.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.external.AbstractSimpleDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureBlobContainer;
import no.fintlabs.azure.StorageAccountUtilities;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.fintlabs.azure.StorageAccountUtilities.sanitizeStorageAccountName;

@Slf4j
@Component
public class AzureStorageContainerDependentResource
        extends PerResourcePollingDependentResource<AzureBlobContainer, AzureStorageBlobCrd>
        implements EventSourceProvider<AzureStorageBlobCrd>,
        Creator<AzureBlobContainer, AzureStorageBlobCrd>,
        Deleter<AzureStorageBlobCrd> {

    @Value("${fint.azure.storage.resource-group-name:rg-storage}")
    private String resourceGroup;
    private final StorageManager storageManager;

    public AzureStorageContainerDependentResource(StorageManager storageManager, AzureStorageBlobWorkflow workflow) {
        super(AzureBlobContainer.class, Duration.ofDays(1).toMillis());
        this.storageManager = storageManager;
        workflow.addDependentResource(this);
    }

    @Override
    protected AzureBlobContainer desired(AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        return AzureBlobContainer.builder()
                .blobContainerName(RandomStringUtils.randomAlphabetic(6))
                .resourceGroup(resourceGroup)
                .storageAccountName(primary.getMetadata().getName())
                .build();
    }

    @Override
    public void delete(AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        context.getSecondaryResource(AzureBlobContainer.class)
                .ifPresent(azureBlobContainer -> storageManager
                        .storageAccounts()
                        .deleteByResourceGroup(resourceGroup, azureBlobContainer.getStorageAccountName())
                );
    }

    @Override
    public AzureBlobContainer create(AzureBlobContainer desired, AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        log.info("Creating storage account...");
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(sanitizeStorageAccountName(primary.getMetadata().getName()))
                .withRegion(Region.NORWAY_EAST)
                .withExistingResourceGroup(resourceGroup)
                .withGeneralPurposeAccountKindV2()
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withBlobStorageAccountKind()
                .withAccessTier(AccessTier.HOT)
                //.withAccessFromAzureServices()
                .disableBlobPublicAccess()
                .create();

        log.info("Storage account created. Status is: {}", storageAccount.accountStatuses().primary().name());
        log.info("Creating blob container...");
        String blobContainerName = RandomStringUtils.randomAlphabetic(4).toLowerCase();
        BlobContainer blobContainer = storageAccount
                .manager()
                .blobContainers().defineContainer(blobContainerName)
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .create();
        log.info("Blob container created: {}", blobContainer.innerModel());

        return AzureBlobContainer.builder()
                .blobContainerName(blobContainerName)
                .resourceGroup(resourceGroup)
                .storageAccountName(storageAccount.name())
                .connectionString(StorageAccountUtilities.getConnectionString(storageAccount))
                .build();
    }

    @Override
    public Set<AzureBlobContainer> fetchResources(AzureStorageBlobCrd primaryResource) {
        log.info("Fetching Azure blob container...");
        CheckNameAvailabilityResult checkNameAvailabilityResult = storageManager.storageAccounts().checkNameAvailability(sanitizeStorageAccountName(primaryResource.getMetadata().getName()));
        if (checkNameAvailabilityResult.isAvailable()) {
            return Collections.emptySet();
        }
        StorageAccount storageAccount = storageManager.storageAccounts().getByResourceGroup(resourceGroup, sanitizeStorageAccountName(primaryResource.getMetadata().getName()));
        List<ListContainerItemInner> list = storageAccount
                .manager()
                .blobContainers()
                .list(resourceGroup, sanitizeStorageAccountName(primaryResource.getMetadata().getName()))
                .stream().toList();

        return Collections.singleton(AzureBlobContainer.builder()
                .storageAccountName(storageAccount.name())
                .resourceGroup(storageAccount.resourceGroupName())
                .blobContainerName(list.isEmpty() ? "" : list.get(0).name())
                .connectionString(StorageAccountUtilities.getConnectionString(storageAccount))
                .build());
    }
}
