package no.fintlabs.azure.storage.fileshare;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import no.fintlabs.azure.AzureCrd;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("AzureFileShare")
public class FileShareCrd extends AzureCrd<FileShareSpec, FileShareStatus> implements Namespaced {
    @Override
    protected FileShareStatus initStatus() {
        return new FileShareStatus();
    }

    @Override
    protected FileShareSpec initSpec() {
        return new FileShareSpec();
    }


}
