package no.fintlabs.common;

import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.ReconcileResult;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <S> Status object
 * @param <R> Custom resource definition
 * @param <T> Spec object
 */
public interface ReconcileHandler<S extends FlaisStatus, R extends FlaisCrd<T>, T extends FlaisSpec> {

    default boolean hasUpdates(WorkflowReconcileResult reconcile) {
        return reconcile.getReconcileResults().values().stream().map(reconcileResult -> reconcileResult.getOperation().name())
                .filter(s -> s.equals(ReconcileResult.Operation.NONE.name()))
                .count() != reconcile.getReconciledDependents().size();
    }

    default boolean isCreated(WorkflowReconcileResult reconcile) {
        return reconcile.getReconcileResults().values().stream().map(reconcileResult -> reconcileResult.getOperation().name())
                .filter(s -> s.equals(ReconcileResult.Operation.CREATED.name()))
                .count() == reconcile.getReconciledDependents().size();
    }

    default S createStatus(WorkflowReconcileResult reconcile, S status) {
        List<String> results = new ArrayList<>();
        reconcile.getReconcileResults()
                .forEach((dependentResource, reconcileResult) -> results.add(dependentResource.resourceType().getSimpleName() + " -> " + reconcileResult.getOperation().name()));
        status.setDependentResourceStatus(results);

        return status;

    }

    default UpdateControl<R> updateStatus(WorkflowReconcileResult reconcile, R resource, S status) {
        if (isCreated(reconcile)) {
            resource.setStatus(createStatus(reconcile, status));
            return UpdateControl.updateStatus(resource);
        }

        if (hasUpdates(reconcile)) {

            resource.setStatus(createStatus(reconcile, status));
            return UpdateControl.patchStatus(resource);
        }

        return UpdateControl.noUpdate();
    }
}
