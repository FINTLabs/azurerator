package no.fintlabs;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Workflow;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
//@Component
@ControllerConfiguration(
        generationAwareEventProcessing = false
)
public class BlobContainerReconiler implements Reconciler<AzureStorageBlobCrd>,
        ErrorStatusHandler<AzureStorageBlobCrd>,
        EventSourceInitializer<AzureStorageBlobCrd> {

    private final AzureStorageBlobWorkflow workflow;
    private final List<? extends CRUDKubernetesDependentResource<?, AzureStorageBlobCrd>> dependentResources;

    public BlobContainerReconiler(AzureStorageBlobWorkflow workflow, List<? extends CRUDKubernetesDependentResource<?, AzureStorageBlobCrd>> dependentResources) {
        this.workflow = workflow;
        this.dependentResources = dependentResources;
    }

    @Override
    public UpdateControl<AzureStorageBlobCrd> reconcile(AzureStorageBlobCrd resource, Context<AzureStorageBlobCrd> context) {

        CrdValidator.validate(resource);

//        Workflow<AzureStorageBlobCrd> flaisApplicationCrdWorkflow = workflow.build();
//        log.info("Reconciling {} dependent resources", flaisApplicationCrdWorkflow.getDependentResources().size());
//        WorkflowReconcileResult reconcile = flaisApplicationCrdWorkflow.reconcile(resource, context);


        //resource.setStatus(createStatus(reconcile));
        return UpdateControl.updateStatus(resource);
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
        EventSource[] eventSources = dependentResources
                .stream()
                .map(crudKubernetesDependentResource -> crudKubernetesDependentResource.initEventSource(context))
                .toArray(EventSource[]::new);
        return EventSourceInitializer.nameEventSources(eventSources);
    }
}
