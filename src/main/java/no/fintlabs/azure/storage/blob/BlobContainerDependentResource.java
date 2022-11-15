package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.StorageResource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class BlobContainerDependentResource
        extends FlaisExternalDependentResource<StorageResource, BlobContainerCrd, BlobContainerSpec> {


    private final BlobContainerService blobContainerService;

    public BlobContainerDependentResource(FlaisWorkflow<BlobContainerCrd, BlobContainerSpec> workflow,
                                          BlobContainerService blobContainerService, AzureConfiguration azureConfiguration) {
        super(StorageResource.class, workflow);
        this.blobContainerService = blobContainerService;
        setPollingPeriod(Duration.ofMinutes(azureConfiguration.getStorageAccountPollingPeriodInMinutes()).toMillis());
    }

    @Override
    protected StorageResource desired(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return StorageResource.desired();
    }

    @Override
    public void delete(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        context.getSecondaryResource(StorageResource.class)
                .ifPresent(blobContainerService::delete);
    }

    @Override
    public StorageResource create(StorageResource desired, BlobContainerCrd primary, Context<BlobContainerCrd> context) {

        return blobContainerService.add(desired, primary);

    }

    @Override
    public Set<StorageResource> fetchResources(BlobContainerCrd primaryResource) {
        return blobContainerService.get(primaryResource);
    }
}
