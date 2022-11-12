package no.fintlabs.azure.storage.blob;

import lombok.*;
import no.fintlabs.azure.AzureSpec;
import no.fintlabs.azure.Defaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobContainerSpec implements AzureSpec {
    @Builder.Default
    private String resourceGroup = Defaults.RESOURCE_GROUP;
}
