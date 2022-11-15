package no.fintlabs.azure.storage.fileshare;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.storage.AzureStorageObject;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class FileShareDependentResource extends FlaisExternalDependentResource<AzureStorageObject, FileShareCrd, FileShareSpec> {


    private final FileShareService fileShareService;


    public FileShareDependentResource(FileShareWorkflow workflow,
                                      FileShareService fileShareService, AzureConfiguration azureConfiguration) {
        super(AzureStorageObject.class, workflow);
        this.fileShareService = fileShareService;
        setPollingPeriod(Duration.ofMinutes(azureConfiguration.getStorageAccountPollingPeriodInMinutes()).toMillis());
    }


    @Override
    protected AzureStorageObject desired(FileShareCrd primary, Context<FileShareCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return AzureStorageObject.desired();
    }

    @Override
    public void delete(FileShareCrd primary, Context<FileShareCrd> context) {
        try {
            context.getSecondaryResource(AzureStorageObject.class)
                    .ifPresent(fileShareService::delete);
        } catch (IllegalArgumentException e) {
            log.error("An error occurred when deleting {}", primary.getMetadata().getName());
            log.error("Error message is {}", e.getMessage());
        }
    }

    @Override
    public AzureStorageObject create(AzureStorageObject desired, FileShareCrd primary, Context<FileShareCrd> context) {
        return fileShareService.add(desired,primary);
    }

    @Override
    public Set<AzureStorageObject> fetchResources(FileShareCrd primaryResource) {
        return fileShareService.get(primaryResource);
    }

}
