package no.fintlabs.azure.storage.fileshare;

import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.common.FlaisReconiler;
import no.fintlabs.common.FlaisWorkflow;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ControllerConfiguration(
        generationAwareEventProcessing = false
)
public class FileShareReconiler extends FlaisReconiler<FileShareCrd, FileShareSpec> {
    public FileShareReconiler(FlaisWorkflow<FileShareCrd, FileShareSpec> workflow,
                              List<? extends EventSourceProvider<FileShareCrd>> eventSourceProviders,
                              List<? extends Deleter<FileShareCrd>> deleters) {
        super(workflow, eventSourceProviders, deleters);
    }
}
