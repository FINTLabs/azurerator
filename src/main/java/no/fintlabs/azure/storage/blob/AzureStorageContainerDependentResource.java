package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class AzureStorageContainerDependentResource
        extends PerResourcePollingDependentResource<AzureBlobContainer, AzureStorageBlobCrd>
        implements EventSourceProvider<AzureStorageBlobCrd>,
        Creator<AzureBlobContainer, AzureStorageBlobCrd>,
        Deleter<AzureStorageBlobCrd> {

    @Value("${fint.azure.storage.resource-group-name:rg-storage}")
    private String resourceGroup;

    private final BlobContainerService blobContainerService;

    public AzureStorageContainerDependentResource(AzureStorageBlobWorkflow workflow,
                                                  BlobContainerService blobContainerService) {
        super(AzureBlobContainer.class, Duration.ofMinutes(1).toMillis());
        this.blobContainerService = blobContainerService;
        workflow.addDependentResource(this);
    }

    @Override
    protected AzureBlobContainer desired(AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return AzureBlobContainer.builder()
                .blobContainerName(RandomStringUtils.randomAlphabetic(6))
                .resourceGroup(resourceGroup)
                .storageAccountName(primary.getMetadata().getName())
                .build();
    }

    @Override
    public void delete(AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        context.getSecondaryResource(AzureBlobContainer.class)
                .ifPresent(blobContainerService::delete);
    }

    @Override
    public AzureBlobContainer create(AzureBlobContainer desired, AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {

        return blobContainerService.add(primary);

    }

    @Override
    public Set<AzureBlobContainer> fetchResources(AzureStorageBlobCrd primaryResource) {
        return blobContainerService.get(primaryResource);
    }
}
