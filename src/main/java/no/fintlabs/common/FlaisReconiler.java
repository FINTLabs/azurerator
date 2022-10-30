package no.fintlabs.common;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Workflow;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.CrdValidator;

import java.util.List;
import java.util.Map;

/**
 * @param <T> the class representing the CustomResource object
 * @param <S> the class providing the {@code Spec} part of this CustomResource
 */
@Slf4j
public class FlaisReconiler<T extends FlaisCrd<S>, S extends FlaisSpec>
        implements Reconciler<T>,
        Cleaner<T>,
        ErrorStatusHandler<T>,
        EventSourceInitializer<T>,
        ReconcileHandler<FlaisStatus, T, S>
{

    private final FlaisWorkflow<T, S> workflow;
    private final List<? extends EventSourceProvider<T>> eventSourceProviders;
    private final List<? extends Deleter<T>> deleters;

    public FlaisReconiler(FlaisWorkflow<T, S> workflow,
                          List<? extends EventSourceProvider<T>> eventSourceProviders,
                          List<? extends Deleter<T>> deleters) {
        this.workflow = workflow;
        this.eventSourceProviders = eventSourceProviders;
        this.deleters = deleters;
    }


    @Override
    public UpdateControl<T> reconcile(T resource,
                                      Context<T> context) {

        CrdValidator.validate(resource);

        Workflow<T> fileShareWorkflow = workflow.build();
        log.debug("Reconciling {} dependent resources", fileShareWorkflow.getDependentResources().size());
        WorkflowReconcileResult reconcile = fileShareWorkflow.reconcile(resource, context);

        return updateStatus(reconcile, resource, new FlaisStatus());
    }

    @Override
    public DeleteControl cleanup(T resource, Context<T> context) {
        deleters.forEach(dr -> dr.delete(resource, context));
        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<T> updateErrorStatus(T resource, Context<T> context, Exception e) {
        FlaisStatus fileShareStatus = new FlaisStatus();
        fileShareStatus.setErrorMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        resource.setStatus(fileShareStatus);
        return ErrorStatusUpdateControl.updateStatus(resource);
    }

    @Override
    public Map<String, EventSource> prepareEventSources(EventSourceContext<T> context) {
        EventSource[] eventSources = eventSourceProviders
                .stream()
                .map(dr -> dr.initEventSource(context))
                .toArray(EventSource[]::new);
        return EventSourceInitializer.nameEventSources(eventSources);
    }


}
