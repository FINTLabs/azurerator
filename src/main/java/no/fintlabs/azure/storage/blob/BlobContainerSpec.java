package no.fintlabs.azure.storage.blob;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobContainerSpec {
    @Builder.Default
    private String resourceGroup = "rg-storage";
}
