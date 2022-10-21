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
public class BlobContainerReconiler implements Reconciler<AzureStorageBlobCrd>,
        Cleaner<AzureStorageBlobCrd>,
        ErrorStatusHandler<AzureStorageBlobCrd>,
        EventSourceInitializer<AzureStorageBlobCrd> {

    private final AzureStorageBlobWorkflow workflow;
    private final List<? extends EventSourceProvider<AzureStorageBlobCrd>> eventSourceProviders;
    private final List<? extends Deleter<AzureStorageBlobCrd>> deleters;

    public BlobContainerReconiler(AzureStorageBlobWorkflow workflow,
                                  List<? extends EventSourceProvider<AzureStorageBlobCrd>> eventSourceProviders,
                                  List<? extends Deleter<AzureStorageBlobCrd>> deleters) {
        this.workflow = workflow;
        this.eventSourceProviders = eventSourceProviders;
        this.deleters = deleters;
    }


    @Override
    public UpdateControl<AzureStorageBlobCrd> reconcile(AzureStorageBlobCrd resource,
                                                        Context<AzureStorageBlobCrd> context) {

        CrdValidator.validate(resource);

        Workflow<AzureStorageBlobCrd> flaisApplicationCrdWorkflow = workflow.build();
        log.debug("Reconciling {} dependent resources", flaisApplicationCrdWorkflow.getDependentResources().size());
        WorkflowReconcileResult reconcile = flaisApplicationCrdWorkflow.reconcile(resource, context);


        List<String> results = new ArrayList<>();
        reconcile.getReconcileResults().forEach((dependentResource, reconcileResult) -> results.add(dependentResource.toString() + " -> " + reconcileResult.getOperation().name()));
        resource.setStatus(AzureStorageBlobStatus
                .builder()
                .dependentResourceStatus(results)
                .build()
        );
        return UpdateControl.patchStatus(resource);
    }

    @Override
    public DeleteControl cleanup(AzureStorageBlobCrd resource, Context<AzureStorageBlobCrd> context) {
        deleters.forEach(dr -> dr.delete(resource, context));
        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<AzureStorageBlobCrd> updateErrorStatus(AzureStorageBlobCrd resource, Context<AzureStorageBlobCrd> context, Exception e) {
        AzureStorageBlobStatus flaisApplicationStatus = new AzureStorageBlobStatus();
        flaisApplicationStatus.setErrorMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        resource.setStatus(flaisApplicationStatus);
        return ErrorStatusUpdateControl.updateStatus(resource);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<AzureStorageBlobCrd> context) {
        EventSource[] eventSources = eventSourceProviders
                .stream()
                .map(dr -> dr.initEventSource(context))
                .toArray(EventSource[]::new);
        return EventSourceInitializer.nameEventSources(eventSources);
    }


}
