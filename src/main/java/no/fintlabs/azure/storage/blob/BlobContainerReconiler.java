package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.*;
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
        ErrorStatusHandler<AzureStorageBlobCrd>,
        EventSourceInitializer<AzureStorageBlobCrd> {

    private final AzureStorageBlobWorkflow workflow;
    private final AzureStorageContainerDependentResource azureStorageContainerDependentResource;
    private final List<? extends EventSourceProvider<AzureStorageBlobCrd>> dependentResources;

    public BlobContainerReconiler(AzureStorageBlobWorkflow workflow, /*,List<? extends CRUDKubernetesDependentResource<?, AzureStorageBlobCrd>> dependentResources*/AzureStorageContainerDependentResource azureStorageContainerDependentResource, List<? extends EventSourceProvider<AzureStorageBlobCrd>> dependentResources) {
        this.workflow = workflow;
        //this.dependentResources = dependentResources;
        this.azureStorageContainerDependentResource = azureStorageContainerDependentResource;
        this.dependentResources = dependentResources;

        //dependentResources.forEach(workflow::addDependentResource);
    }


    @Override
    public UpdateControl<AzureStorageBlobCrd> reconcile(AzureStorageBlobCrd resource, Context<AzureStorageBlobCrd> context) {

        CrdValidator.validate(resource);

        Workflow<AzureStorageBlobCrd> flaisApplicationCrdWorkflow = workflow.build();
        log.info("Reconciling {} dependent resources", flaisApplicationCrdWorkflow.getDependentResources().size());
        WorkflowReconcileResult reconcile = flaisApplicationCrdWorkflow.reconcile(resource, context);


        List<String> results = new ArrayList<>();
        reconcile.getReconcileResults().forEach((dependentResource, reconcileResult) -> results.add(dependentResource.toString() + " -> " + reconcileResult.getOperation().name()));
        resource.setStatus(AzureStorageBlobStatus
                .builder()
                .dependentResourceStatus(results)
                .build()
        );
        return UpdateControl.updateResource(resource);
    }


    @Override
    public ErrorStatusUpdateControl<AzureStorageBlobCrd> updateErrorStatus(AzureStorageBlobCrd resource, Context<AzureStorageBlobCrd> context, Exception e) {
        AzureStorageBlobStatus flaisApplicationStatus = new AzureStorageBlobStatus();
        flaisApplicationStatus.setErrorMessage(e.getCause().getMessage());
        resource.setStatus(flaisApplicationStatus);
        return ErrorStatusUpdateControl.updateStatus(resource);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<AzureStorageBlobCrd> context) {
        List<EventSource> collect = dependentResources
                .stream()
                .map(dr -> (dr).initEventSource(context)).toList();
        return EventSourceInitializer
                .nameEventSources(collect.toArray(EventSource[]::new));

    }

//    @Override
//    public Map<String, EventSource> prepareEventSources(EventSourceContext<AzureStorageBlobCrd> context) {
////        EventSource[] eventSources = dependentResources
////                .stream()
////                .map(crudKubernetesDependentResource -> crudKubernetesDependentResource.initEventSource(context))
////                .toArray(EventSource[]::new);
//        return EventSourceInitializer.nameEventSources(/*eventSources*/);
//    }
}
