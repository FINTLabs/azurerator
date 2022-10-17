package no.fintlabs;

import io.javaoperatorsdk.operator.api.ObservedGenerationAware;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public
class AzureStorageBlobStatus implements ObservedGenerationAware {
    private List<String> dependentResourceStatus = new ArrayList<>();
    private String errorMessage;
    private Long observedGeneration;


    @Override
    public void setObservedGeneration(Long generation) {
        observedGeneration = generation;
    }

    @Override
    public Long getObservedGeneration() {
        return observedGeneration;
    }
}
