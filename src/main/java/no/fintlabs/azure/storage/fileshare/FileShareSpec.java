package no.fintlabs.azure.storage.fileshare;

import lombok.*;
import no.fintlabs.azure.AzureSpec;
import no.fintlabs.azure.Defaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileShareSpec implements AzureSpec {
    @Builder.Default
    private String resourceGroup = Defaults.RESOURCE_GROUP;
}
