package no.fintlabs.azure.storage.fileshare;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisExternalDependentResource;
import no.fintlabs.azure.AzureConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class FileShareDependentResource extends FlaisExternalDependentResource<FileShare, FileShareCrd, FileShareSpec> {


    private final FileShareService fileShareService;
    private final AzureConfiguration azureConfiguration;


    public FileShareDependentResource(FileShareWorkflow workflow,
                                      FileShareService fileShareService, AzureConfiguration azureConfiguration) {
        super(FileShare.class, workflow);
        this.fileShareService = fileShareService;
        this.azureConfiguration = azureConfiguration;
        setPollingPeriod(Duration.ofMinutes(azureConfiguration.getStorageAccountPollingPeriodInMinutes()).toMillis());
    }


    @Override
    protected FileShare desired(FileShareCrd primary, Context<FileShareCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return FileShare.builder()
                .resourceGroup(azureConfiguration.getStorageAccountResourceGroup())
                .shareName(RandomStringUtils.randomAlphabetic(12).toLowerCase())
                //.storageAccountName(primary.getMetadata().getName())
                .build();
    }

    @Override
    public void delete(FileShareCrd primary, Context<FileShareCrd> context) {
        try {
            context.getSecondaryResource(FileShare.class)
                    .ifPresent(fileShareService::delete);
        } catch (IllegalArgumentException e) {
            log.error("An error occurred when deleting {}", primary.getMetadata().getName());
            log.error("Error message is {}", e.getMessage());
            log.error("This is probably because we were not able to fetch the storage account from Azure. Probably" +
                    " because it does not exist. You can most likely ignore this error ;)");
        }
    }

    @Override
    public FileShare create(FileShare desired, FileShareCrd primary, Context<FileShareCrd> context) {
        return fileShareService.add(desired,primary);
    }

    @Override
    public Set<FileShare> fetchResources(FileShareCrd primaryResource) {
        return fileShareService.get(primaryResource);
    }

}
