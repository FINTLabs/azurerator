package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
//@Component
public class BlobContainerDependentResource
        extends PerResourcePollingDependentResource<BlobContainer, BlobContainerCrd>
        implements EventSourceProvider<BlobContainerCrd>,
        Creator<BlobContainer, BlobContainerCrd>,
        Deleter<BlobContainerCrd> {


    private final BlobContainerService blobContainerService;

    public BlobContainerDependentResource(BlobContainerWorkflow workflow,
                                          BlobContainerService blobContainerService) {
        super(BlobContainer.class, Duration.ofMinutes(10).toMillis());
        this.blobContainerService = blobContainerService;
        workflow.addDependentResource(this);
    }

    @Override
    protected BlobContainer desired(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return BlobContainer.builder()
                .blobContainerName(RandomStringUtils.randomAlphabetic(6))
                .resourceGroup(primary.getSpec().getResourceGroup())
                .storageAccountName(primary.getMetadata().getName())
                .build();
    }

    @Override
    public void delete(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        context.getSecondaryResource(BlobContainer.class)
                .ifPresent(blobContainerService::delete);
    }

    @Override
    public BlobContainer create(BlobContainer desired, BlobContainerCrd primary, Context<BlobContainerCrd> context) {

        return blobContainerService.add(primary);

    }

    @Override
    public Set<BlobContainer> fetchResources(BlobContainerCrd primaryResource) {
        return blobContainerService.get(primaryResource);
    }
}
