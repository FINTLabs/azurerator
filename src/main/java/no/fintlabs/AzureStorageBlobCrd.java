package no.fintlabs;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("AzureBlobContainer")
public class AzureStorageBlobCrd extends CustomResource<AzureStorageBlobSpec, AzureStorageBlobStatus> implements Namespaced {
}
