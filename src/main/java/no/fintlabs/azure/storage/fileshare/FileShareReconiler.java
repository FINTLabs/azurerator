package no.fintlabs.azure.storage.fileshare;

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
public class FileShareReconiler implements Reconciler<FileShareCrd>,
        Cleaner<FileShareCrd>,
        ErrorStatusHandler<FileShareCrd>,
        EventSourceInitializer<FileShareCrd> {

    private final FileShareWorkflow workflow;
    private final List<? extends EventSourceProvider<FileShareCrd>> eventSourceProviders;
    private final List<? extends Deleter<FileShareCrd>> deleters;

    public FileShareReconiler(FileShareWorkflow workflow,
                              List<? extends EventSourceProvider<FileShareCrd>> eventSourceProviders,
                              List<? extends Deleter<FileShareCrd>> deleters) {
        this.workflow = workflow;
        this.eventSourceProviders = eventSourceProviders;
        this.deleters = deleters;
    }


    @Override
    public UpdateControl<FileShareCrd> reconcile(FileShareCrd resource,
                                                 Context<FileShareCrd> context) {

        CrdValidator.validate(resource);

        Workflow<FileShareCrd> fileShareWorkflow = workflow.build();
        log.debug("Reconciling {} dependent resources", fileShareWorkflow.getDependentResources().size());
        WorkflowReconcileResult reconcile = fileShareWorkflow.reconcile(resource, context);


        List<String> results = new ArrayList<>();
        reconcile.getReconcileResults().forEach((dependentResource, reconcileResult) -> results.add(dependentResource.toString() + " -> " + reconcileResult.getOperation().name()));

        FileShareStatus fileShareStatus = new FileShareStatus();
        fileShareStatus.setDependentResourceStatus(results);
        resource.setStatus(fileShareStatus);
        return UpdateControl.patchStatus(resource);
    }

    @Override
    public DeleteControl cleanup(FileShareCrd resource, Context<FileShareCrd> context) {
        deleters.forEach(dr -> dr.delete(resource, context));
        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<FileShareCrd> updateErrorStatus(FileShareCrd resource, Context<FileShareCrd> context, Exception e) {
        FileShareStatus fileShareStatus = new FileShareStatus();
        fileShareStatus.setErrorMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        resource.setStatus(fileShareStatus);
        return ErrorStatusUpdateControl.updateStatus(resource);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<FileShareCrd> context) {
        EventSource[] eventSources = eventSourceProviders
                .stream()
                .map(dr -> dr.initEventSource(context))
                .toArray(EventSource[]::new);
        return EventSourceInitializer.nameEventSources(eventSources);
    }


}
