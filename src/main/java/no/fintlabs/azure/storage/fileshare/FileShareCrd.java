package no.fintlabs.azure.storage.fileshare;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import no.fintlabs.azure.AzureCrd;
import no.fintlabs.common.FlaisCrd;
import no.fintlabs.common.FlaisStatus;

@Group("fintlabs.no")
@Version("v1alpha1")
@Kind("AzureFileShare")
public class FileShareCrd extends AzureCrd<FileShareSpec> implements Namespaced {
    @Override
    protected FlaisStatus initStatus() {
        return new FlaisStatus();
    }

    @Override
    protected FileShareSpec initSpec() {
        return new FileShareSpec();
    }


}
