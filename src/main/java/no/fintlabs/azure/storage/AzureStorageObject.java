package no.fintlabs.azure.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AzureStorageObject {

    protected String storageAccountName;
    protected String resourceGroup;
    @JsonIgnore
    protected String connectionString;

    @Override
    public String toString() {
        return "AzureBlobContainer{" +
                "storageAccountName='" + storageAccountName + '\'' +
                ", resourceGroup='" + resourceGroup + '\'' +
                '}';
    }
}