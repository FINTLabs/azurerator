package no.fintlabs.common;

import io.javaoperatorsdk.operator.processing.dependent.workflow.builder.WorkflowBuilder;
import no.fintlabs.azure.storage.fileshare.FileShareCrd;
import org.springframework.stereotype.Component;

/**
 *
 * @param <T> the class providing the CustomResource
 * @param <S> the class providing the {@code Spec} part of this CustomResource
 */
public abstract class FlaisWorkflow<T extends FlaisCrd<S>, S extends FlaisSpec> extends WorkflowBuilder<T> {

}
