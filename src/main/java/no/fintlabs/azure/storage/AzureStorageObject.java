package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import no.fintlabs.EnvironmentService;
import no.fintlabs.SpringContext;

import static no.fintlabs.azure.TagNames.TAG_ORG_ID;
import static no.fintlabs.azure.TagNames.TAG_TEAM;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AzureStorageObject {

    protected String storageAccountName;
    protected String resourceGroup;
    protected String connectionString;
    protected String status;
    protected String team;
    protected String orgId;
    protected String portalUri;
    protected String environment;

    public static AzureStorageObject of(StorageAccount storageAccount) {

        return AzureStorageObject.builder()
                .storageAccountName(storageAccount.name())
                .resourceGroup(storageAccount.resourceGroupName())
                .connectionString(buildConnectionString(storageAccount))
                .status(storageAccount.accountStatuses().primary().name())
                .team(storageAccount.tags().getOrDefault(TAG_TEAM, "N/A"))
                .orgId(storageAccount.tags().getOrDefault(TAG_ORG_ID, "N/A"))
                .portalUri(buildPortalUri(storageAccount))
                .environment(SpringContext.getBean(EnvironmentService.class).getEnvironment())
                .build();
    }

    private static String buildConnectionString(StorageAccount storageAccount) {
        return String.format(
                "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                storageAccount.name(),
                storageAccount.getKeys().get(0).value()
        );
    }

    private static String buildPortalUri(StorageAccount storageAccount) {
        return String.format("https://portal.azure.com/#@vigoiks.onmicrosoft.com/resource%s", storageAccount.id());
    }
    @Override
    public String toString() {
        return "AzureBlobContainer{" +
                "storageAccountName='" + storageAccountName + '\'' +
                ", resourceGroup='" + resourceGroup + '\'' +
                '}';
    }
}
