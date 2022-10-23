package no.fintlabs.azure.storage.blob;

import lombok.*;
import no.fintlabs.azure.AzureSpec;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobContainerSpec implements AzureSpec {
    @Builder.Default
    private String resourceGroup = "rg-storage";
}
