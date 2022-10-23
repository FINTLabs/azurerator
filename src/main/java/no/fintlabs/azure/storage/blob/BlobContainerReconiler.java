package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Workflow;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.CrdValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ControllerConfiguration(
        generationAwareEventProcessing = false
)
public class BlobContainerReconiler implements Reconciler<BlobContainerCrd>,
        Cleaner<BlobContainerCrd>,
        ErrorStatusHandler<BlobContainerCrd>,
        EventSourceInitializer<BlobContainerCrd> {


    private final BlobContainerWorkflow workflow;
    private final List<? extends EventSourceProvider<BlobContainerCrd>> eventSourceProviders;
    private final List<? extends Deleter<BlobContainerCrd>> deleters;

    public BlobContainerReconiler(BlobContainerWorkflow workflow,
                                  List<? extends EventSourceProvider<BlobContainerCrd>> eventSourceProviders,
                                  List<? extends Deleter<BlobContainerCrd>> deleters) {
        this.workflow = workflow;
        this.eventSourceProviders = eventSourceProviders;
        this.deleters = deleters;
    }


    @Override
    public UpdateControl<BlobContainerCrd> reconcile(BlobContainerCrd resource,
                                                     Context<BlobContainerCrd> context) {

        CrdValidator.validate(resource);

        Workflow<BlobContainerCrd> blobContainerWorkflow = workflow.build();
        log.debug("Reconciling {} dependent resources", blobContainerWorkflow.getDependentResources().size());
        WorkflowReconcileResult reconcile = blobContainerWorkflow.reconcile(resource, context);



        List<String> results = new ArrayList<>();
        reconcile.getReconcileResults().forEach((dependentResource, reconcileResult) -> results.add(dependentResource.toString() + " -> " + reconcileResult.getOperation().name()));

        BlobContainerStatus blobContainerStatus = new BlobContainerStatus();
        blobContainerStatus.setDependentResourceStatus(results);
        resource.setStatus(blobContainerStatus);
        return UpdateControl.patchStatus(resource);
    }


    @Override
    public DeleteControl cleanup(BlobContainerCrd resource, Context<BlobContainerCrd> context) {
        deleters.forEach(dr -> dr.delete(resource, context));
        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<BlobContainerCrd> updateErrorStatus(BlobContainerCrd resource, Context<BlobContainerCrd> context, Exception e) {
        BlobContainerStatus flaisApplicationStatus = new BlobContainerStatus();
        flaisApplicationStatus.setErrorMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        resource.setStatus(flaisApplicationStatus);
        return ErrorStatusUpdateControl.updateStatus(resource);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<BlobContainerCrd> context) {
        EventSource[] eventSources = eventSourceProviders
                .stream()
                .map(dr -> dr.initEventSource(context))
                .toArray(EventSource[]::new);
        return EventSourceInitializer.nameEventSources(eventSources);
    }


}
