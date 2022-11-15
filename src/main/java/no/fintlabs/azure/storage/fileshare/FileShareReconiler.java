package no.fintlabs.azure.storage.fileshare;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisReconiler;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ControllerConfiguration(
        generationAwareEventProcessing = false
)
public class FileShareReconiler extends FlaisReconiler<FileShareCrd, FileShareSpec> {

    public FileShareReconiler(FileShareWorkflow workflow,
                              List<? extends DependentResource<?, FileShareCrd>> eventSourceProviders,
                              List<? extends Deleter<FileShareCrd>> deleters) {
        super(workflow, eventSourceProviders, deleters);
    }

    @Override
    public UpdateControl<FileShareCrd> reconcile(FileShareCrd resource, Context<FileShareCrd> context) {
        return super.reconcile(resource, context);
    }
}
