package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.azure.AzureConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class BlobContainerDependentResource
        extends FlaisExternalDependentResource<BlobContainer, BlobContainerCrd, BlobContainerSpec> {


    private final BlobContainerService blobContainerService;

    private final AzureConfiguration azureConfiguration;

    public BlobContainerDependentResource(FlaisWorkflow<BlobContainerCrd, BlobContainerSpec> workflow,
                                          BlobContainerService blobContainerService, AzureConfiguration azureConfiguration) {
        super(BlobContainer.class, workflow);
        this.blobContainerService = blobContainerService;
        this.azureConfiguration = azureConfiguration;
        setPollingPeriod(Duration.ofMinutes(10).toMillis());
    }

    @Override
    protected BlobContainer desired(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return BlobContainer.builder()
                .blobContainerName(RandomStringUtils.randomAlphabetic(12).toLowerCase())
                .resourceGroup(azureConfiguration.getStorageAccountResourceGroup())
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

        return blobContainerService.add(desired, primary);

    }

    @Override
    public Set<BlobContainer> fetchResources(BlobContainerCrd primaryResource) {
        return blobContainerService.get(primaryResource);
    }
}
