package no.fintlabs.azure.storage.blob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
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
