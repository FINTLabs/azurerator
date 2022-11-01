package no.fintlabs.azure.storage.fileshare;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
