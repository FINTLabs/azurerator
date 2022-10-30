package no.fintlabs.azure.storage.fileshare;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.common.FlaisWorkflow;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;


@Slf4j
@Component
public class FileShareDependentResource
        extends PerResourcePollingDependentResource<FileShare, FileShareCrd>
        implements EventSourceProvider<FileShareCrd>,
        Creator<FileShare, FileShareCrd>,
        Deleter<FileShareCrd> {


    private final FileShareService fileShareService;

    public FileShareDependentResource(FlaisWorkflow<FileShareCrd,FileShareSpec> workflow,
                                      FileShareService fileShareService) {
        super(FileShare.class, Duration.ofMinutes(10).toMillis());
        this.fileShareService = fileShareService;
        workflow.addDependentResource(this);
    }

    @Override
    protected FileShare desired(FileShareCrd primary, Context<FileShareCrd> context) {
        log.debug("Desired storage account for {}:", primary.getMetadata().getName());
        log.debug("\t{}", primary);

        return FileShare.builder()
                .resourceGroup(primary.getSpec().getResourceGroup())
                .storageAccountName(primary.getMetadata().getName())
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
        return fileShareService.add(primary);
    }

    @Override
    public Set<FileShare> fetchResources(FileShareCrd primaryResource) {
        return fileShareService.get(primaryResource);
    }

}
