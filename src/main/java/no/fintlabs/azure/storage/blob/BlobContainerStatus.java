package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public
class BlobContainerStatus extends ObservedGenerationAwareStatus {
    private List<String> dependentResourceStatus = new ArrayList<>();
    private String errorMessage;
}
