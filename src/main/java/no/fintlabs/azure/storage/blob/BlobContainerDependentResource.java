package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.FlaisWorkflow;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.AzureStorageObject;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class BlobContainerDependentResource
        extends FlaisExternalDependentResource<AzureStorageObject, BlobContainerCrd, BlobContainerSpec> {


    private final BlobContainerService blobContainerService;

    public BlobContainerDependentResource(FlaisWorkflow<BlobContainerCrd, BlobContainerSpec> workflow,
                                          BlobContainerService blobContainerService, AzureConfiguration azureConfiguration) {
        super(AzureStorageObject.class, workflow);
        this.blobContainerService = blobContainerService;
        setPollingPeriod(Duration.ofMinutes(azureConfiguration.getStorageAccountPollingPeriodInMinutes()).toMillis());
    }

    @Override
    protected AzureStorageObject desired(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return AzureStorageObject.desired();
//                BlobContainer.builder()
//                .blobContainerName(RandomStringUtils.randomAlphabetic(12).toLowerCase())
//                .resourceGroup(azureConfiguration.getStorageAccountResourceGroup())
//                .storageAccountName(primary.getMetadata().getName())
//                .build();
    }

    @Override
    public void delete(BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        context.getSecondaryResource(AzureStorageObject.class)
                .ifPresent(blobContainerService::delete);
    }

    @Override
    public AzureStorageObject create(AzureStorageObject desired, BlobContainerCrd primary, Context<BlobContainerCrd> context) {

        return blobContainerService.add(desired, primary);

    }

    @Override
    public Set<AzureStorageObject> fetchResources(BlobContainerCrd primaryResource) {
        return blobContainerService.get(primaryResource);
    }
}
