package no.fintlabs;

import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import io.javaoperatorsdk.operator.api.ObservedGenerationAware;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public
class AzureStorageBlobStatus implements ObservedGenerationAware {
    private StorageAccountInner storageAccount;
    private String blobContainerName;
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
