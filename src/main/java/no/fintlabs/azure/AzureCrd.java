package no.fintlabs.azure;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;

public abstract class AzureCrd<T extends AzureSpec, P> extends CustomResource<T, P> {
}
