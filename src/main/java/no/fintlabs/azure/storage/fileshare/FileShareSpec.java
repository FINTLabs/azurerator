package no.fintlabs.azure.storage.fileshare;

import lombok.*;
import no.fintlabs.azure.AzureSpec;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileShareSpec implements AzureSpec {
    @Builder.Default
    private String resourceGroup = "rg-storage";
}
