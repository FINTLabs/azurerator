package no.fintlabs.azure.storage.blob;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import no.fintlabs.FlaisCrd;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("AzureBlobContainer")
public class BlobContainerCrd extends FlaisCrd<BlobContainerSpec> implements Namespaced {

    @Override
    protected BlobContainerSpec initSpec() {
        return new BlobContainerSpec();
    }
}
