package no.fintlabs.azure.storage.fileshare;

import lombok.*;
import lombok.experimental.SuperBuilder;
import no.fintlabs.azure.storage.AzureStorageObject;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileShare extends AzureStorageObject {
    private String shareName;
}
