package no.fintlabs.common;

import io.fabric8.kubernetes.client.CustomResource;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.springboot.starter.ResourceClassResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@ConditionalOnProperty(havingValue = "flais.operator.resolve-crd-class", value = "FLAIS", matchIfMissing = true)
public class FlaisResourceClassResolver implements ResourceClassResolver {

    @Override
    @SuppressWarnings("unchecked")
    public <R extends CustomResource<?, ?>> Class<R> resolveCustomResourceClass(Reconciler<?> reconciler) {

        final var type = ResolvableType.forClass(reconciler.getClass());

        return (Class<R>) Arrays.stream(type.getSuperType().getInterfaces())
                .filter(resolvableType -> resolvableType.toClass().getCanonicalName().equals(Reconciler.class.getCanonicalName()))
                .findAny()
                .orElseThrow().resolveGeneric(0);
    }
}
