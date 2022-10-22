package no.fintlabs.azure.storage.blob;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("AzureBlobContainer")
public class BlobContainerCrd extends CustomResource<BlobContainerSpec, BlobContainerStatus> implements Namespaced {
    @Override
    protected BlobContainerStatus initStatus() {
        return new BlobContainerStatus();
    }

    @Override
    protected BlobContainerSpec initSpec() {
        return new BlobContainerSpec();
    }
}
