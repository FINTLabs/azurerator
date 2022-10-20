package no.fintlabs.azure.storage.blob;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=flaiserator")
public class AzureBlobContainerSecretDependentResource
        extends CRUDKubernetesDependentResource<Secret, AzureStorageBlobCrd> {

    public AzureBlobContainerSecretDependentResource(AzureStorageBlobWorkflow workflow, AzureStorageContainerDependentResource azureStorageContainerDependentResource, KubernetesClient kubernetesClient) {

        super(Secret.class);
        workflow.addDependentResource(this).dependsOn(azureStorageContainerDependentResource);
        client = kubernetesClient;
    }


    @Override
    protected Secret desired(AzureStorageBlobCrd resource, Context<AzureStorageBlobCrd> context) {

        log.info("Desired secret for {}", resource.getMetadata().getName());

        Optional<AzureBlobContainer> blobContainer = context.getSecondaryResource(AzureBlobContainer.class);
        AzureBlobContainer azureBlobContainer = blobContainer.orElseThrow();

        HashMap<String, String> labels = new HashMap<>(resource.getMetadata().getLabels());

        labels.put("app.kubernetes.io/managed-by", "flaiserator");
        return new SecretBuilder()
                .withNewMetadata()
                .withName(resource.getMetadata().getName())
                .withNamespace(resource.getMetadata().getNamespace())
                .withLabels(labels)
                .endMetadata()
                .withStringData(new HashMap<>() {{
                    put("fint.azure.storage.container-blob.connection-string", azureBlobContainer.getConnectionString());
                    put("fint.azure.storage.container-blob.container-name", azureBlobContainer.getBlobContainerName());
                }})
                .build();


    }

    // TODO: 18/10/2022 Need to improve matching
    @Override
    public Matcher.Result<Secret> match(Secret actual, AzureStorageBlobCrd primary, Context<AzureStorageBlobCrd> context) {
        final var desiredSecretName = primary.getMetadata().getName();
        return Matcher.Result.nonComputed(actual.getMetadata().getName().equals(desiredSecretName));
    }
}
