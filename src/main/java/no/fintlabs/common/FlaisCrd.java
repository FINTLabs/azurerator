package no.fintlabs.common;

import io.fabric8.kubernetes.client.CustomResource;

/**
 *
 * @param <S> the class providing the {@code Spec} part of this CustomResource
 */
public abstract class FlaisCrd<S extends FlaisSpec> extends CustomResource<S , FlaisStatus> {
}
