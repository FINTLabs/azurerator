package no.fintlabs.azure.storage.blob;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisKubernetesDependentResource;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=flaiserator")
public class BlobContainerSecretDependentResource extends FlaisKubernetesDependentResource<Secret, BlobContainerCrd, BlobContainerSpec> {

    public BlobContainerSecretDependentResource(BlobContainerWorkflow workflow, BlobContainerDependentResource blobContainerDependentResource, KubernetesClient kubernetesClient) {

        super(Secret.class, workflow, kubernetesClient);
        dependsOn(blobContainerDependentResource);
    }


    @Override
    protected Secret desired(BlobContainerCrd resource, Context<BlobContainerCrd> context) {

        log.debug("Desired secret for {}", resource.getMetadata().getName());

        Optional<BlobContainer> blobContainer = context.getSecondaryResource(BlobContainer.class);
        BlobContainer azureBlobContainer = blobContainer.orElseThrow();

        HashMap<String, String> labels = new HashMap<>(resource.getMetadata().getLabels());

        labels.put("app.kubernetes.io/managed-by", "flaiserator");
        return new SecretBuilder()
                .withNewMetadata()
                .withName(resource.getMetadata().getName())
                .withNamespace(resource.getMetadata().getNamespace())
                .withLabels(labels).endMetadata()
                .withType("Opaque")
                .addToData("fint.azure.storage-account.connection-string", encode(azureBlobContainer.getConnectionString()))
                .addToData("fint.azure.storage.container-blob.name", encode((azureBlobContainer.getBlobContainerName())))
                .build();


    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    @Override
    public Matcher.Result<Secret> match(Secret actualResource, BlobContainerCrd primary, Context<BlobContainerCrd> context) {
        Matcher.Result<Secret> match = super.match(actualResource, primary, context);
        log.debug("Secret for {} matched={}", primary.getMetadata().getName(), match.matched());
        return match;
    }
}
