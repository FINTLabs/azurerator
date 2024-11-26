package no.fintlabs.azure.storage.blob;

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
@ControllerConfiguration
public class BlobContainerReconiler extends FlaisReconiler<BlobContainerCrd, BlobContainerSpec> {
    BlobContainerWorkflow workflow;

    public BlobContainerReconiler(BlobContainerWorkflow workflow,
                                  List<? extends DependentResource<?, BlobContainerCrd>> eventSourceProviders,
                                  List<? extends Deleter<BlobContainerCrd>> deleters) {
        super(workflow, eventSourceProviders, deleters);
        this.workflow = workflow;
    }

    @Override
    public UpdateControl<BlobContainerCrd> reconcile(BlobContainerCrd resource, Context<BlobContainerCrd> context) {
        LabelValidator.validate(resource);
        var blobContainerWorkflow = this.workflow.build();
        WorkflowReconcileResult reconcile = blobContainerWorkflow.reconcile(resource, context);

        resource.setStatus(this.createStatus(reconcile, new FlaisStatus()));

        var annotation = context.managedDependentResourceContext().get(ANNOTATION_STORAGE_ACCOUNT_NAME, String.class);
        if (annotation.isPresent()) {
            resource.getMetadata().getAnnotations().put(ANNOTATION_STORAGE_ACCOUNT_NAME, annotation.get());
            return UpdateControl.updateResourceAndStatus(resource);
        }
        return UpdateControl.patchStatus(resource);
    }
}
