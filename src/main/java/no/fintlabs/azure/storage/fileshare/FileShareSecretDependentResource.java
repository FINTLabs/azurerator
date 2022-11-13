package no.fintlabs.azure.storage.fileshare;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.FlaisKubernetesDependentResource;
import no.fintlabs.FlaisWorkflow;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
@KubernetesDependent(labelSelector = "app.kubernetes.io/managed-by=flaiserator")
public class FileShareSecretDependentResource extends FlaisKubernetesDependentResource<Secret, FileShareCrd, FileShareSpec> {

    public FileShareSecretDependentResource(FlaisWorkflow<FileShareCrd, FileShareSpec> workflow, FileShareDependentResource fileShareDependentResource, KubernetesClient kubernetesClient) {

        super(Secret.class, workflow, kubernetesClient);
        dependsOn(fileShareDependentResource);
    }

    @Override
    protected Secret desired(FileShareCrd resource, Context<FileShareCrd> context) {

        log.debug("Desired secret for {}", resource.getMetadata().getName());

        Optional<FileShare> fileShare = context.getSecondaryResource(FileShare.class);
        FileShare azureFileShare = fileShare.orElseThrow();

        HashMap<String, String> labels = new HashMap<>(resource.getMetadata().getLabels());

        labels.put("app.kubernetes.io/managed-by", "flaiserator");
        return new SecretBuilder()
                .withNewMetadata()
                .withName(resource.getMetadata().getName())
                .withNamespace(resource.getMetadata().getNamespace())
                .withLabels(labels)

                .endMetadata()
                .withType("Opaque")
                .addToData("fint.azure.storage-account.connection-string", encode(azureFileShare.getConnectionString()))
                .addToData("fint.azure.storage-account.file-share.name", encode((azureFileShare.getShareName())))
                .build();
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    @Override
    public Matcher.Result<Secret> match(Secret actualResource, FileShareCrd primary, Context<FileShareCrd> context) {
        Matcher.Result<Secret> match = super.match(actualResource, primary, context);
        log.debug("Secret for {} matched={}", primary.getMetadata().getName(), match.matched());
        return match;
    }
}
