package no.fintlabs.azure.storage.fileshare;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisReconiler;
import no.fintlabs.FlaisStatus;
import no.fintlabs.LabelValidator;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.fintlabs.MetadataUtils.ANNOTATION_STORAGE_ACCOUNT_NAME;

@Slf4j
@Component
@ControllerConfiguration(
        generationAwareEventProcessing = false
)
public class FileShareReconiler extends FlaisReconiler<FileShareCrd, FileShareSpec> {
    FileShareWorkflow workflow;

    public FileShareReconiler(FileShareWorkflow workflow,
                              List<? extends DependentResource<?, FileShareCrd>> eventSourceProviders,
                              List<? extends Deleter<FileShareCrd>> deleters) {
        super(workflow, eventSourceProviders, deleters);
        this.workflow = workflow;
    }

    @Override
    public UpdateControl<FileShareCrd> reconcile(FileShareCrd resource, Context<FileShareCrd> context) {
        LabelValidator.validate(resource);
        var fileShareWorkflow = this.workflow.build();
        WorkflowReconcileResult reconcile = fileShareWorkflow.reconcile(resource, context);

        resource.setStatus(this.createStatus(reconcile, new FlaisStatus()));

        var annotation = context.managedDependentResourceContext().get(ANNOTATION_STORAGE_ACCOUNT_NAME, String.class);
        if (annotation.isPresent()) {
            resource.getMetadata().getAnnotations().put(ANNOTATION_STORAGE_ACCOUNT_NAME, annotation.get());
            return UpdateControl.updateResourceAndStatus(resource);
        }
        return UpdateControl.patchStatus(resource);
    }
}
