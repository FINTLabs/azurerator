package no.fintlabs.azure.storage.blob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobContainer {
    private String storageAccountName;
    private String blobContainerName;
    private String resourceGroup;
    @JsonIgnore
    private String connectionString;

    @Override
    public String toString() {
        return "AzureBlobContainer{" +
                "storageAccountName='" + storageAccountName + '\'' +
                ", blobContainerName='" + blobContainerName + '\'' +
                ", resourceGroup='" + resourceGroup + '\'' +
                '}';
    }
}
