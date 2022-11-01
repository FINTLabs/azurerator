package no.fintlabs.azure.storage.blob;

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
public class BlobContainer extends AzureStorageObject {
    private String blobContainerName;
}
