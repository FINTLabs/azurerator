package no.fintlabs;

import com.azure.resourcemanager.storage.models.StorageAccount;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.external.AbstractSimpleDependentResource;
import no.fintlabs.azure.BlobContainerService;
import org.springframework.stereotype.Component;

import java.util.Optional;

//@Component
public class AzureStorageContainerDependentResource
        extends AbstractSimpleDependentResource<StorageAccount, AzureStorageBlobCrd>
        implements Creator<StorageAccount, AzureStorageBlobCrd>,
        Updater<StorageAccount, AzureStorageBlobCrd>,
        Deleter<AzureStorageBlobCrd> {

    private final BlobContainerService blobContainerService;

    public AzureStorageContainerDependentResource(BlobContainerService blobContainerService, AzureStorageBlobWorkflow workflow) {
        this.blobContainerService = blobContainerService;
        workflow.addDependentResource(this);
    }


    @Override
    public StorageAccount create(StorageAccount desired, AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        return null;
    }

    @Override
    public StorageAccount update(StorageAccount actual, StorageAccount desired, AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        return null;
    }

    @Override
    public Optional<StorageAccount> fetchResource(HasMetadata primaryResource) {
        return Optional.empty();
    }

    @Override
    protected void deleteResource(AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {

    }

    @Override
    public Class<StorageAccount> resourceType() {
        return StorageAccount.class;
    }
}
