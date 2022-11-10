package no.fintlabs.azure.storage.blob;

import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.api.reconciler.dependent.EventSourceProvider;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisReconiler;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ControllerConfiguration(
        generationAwareEventProcessing = false
)
public class BlobContainerReconiler extends FlaisReconiler<BlobContainerCrd, BlobContainerSpec> {
    public BlobContainerReconiler(BlobContainerWorkflow workflow,
                                  List<? extends DependentResource<?, BlobContainerCrd>> eventSourceProviders,
                                  List<? extends Deleter<BlobContainerCrd>> deleters) {
        super(workflow, eventSourceProviders, deleters);
    }
}
