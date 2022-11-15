package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import no.fintlabs.EnvironmentService;
import no.fintlabs.SpringContext;
import org.apache.commons.lang3.RandomStringUtils;

import static no.fintlabs.azure.TagNames.TAG_ORG_ID;
import static no.fintlabs.azure.TagNames.TAG_TEAM;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StorageResource {

    protected String storageAccountName;
    protected String resourceGroup;
    protected String connectionString;
    protected String status;
    protected String team;
    protected String orgId;
    protected String portalUri;
    protected String environment;
    protected String path;
    protected StorageType type;


    public static StorageResource desired() {
        return StorageResource.builder()
                .path(RandomStringUtils.randomAlphabetic(12).toLowerCase())
                .build();
    }

    public static StorageResource of(StorageAccount storageAccount) {
        return of(storageAccount, null, StorageType.UNKNOWN);
    }

    public static StorageResource of(StorageAccount storageAccount, String path, StorageType type) {

        return StorageResource.builder()
                .storageAccountName(storageAccount.name())
                .resourceGroup(storageAccount.resourceGroupName())
                .connectionString(buildConnectionString(storageAccount))
                .status(storageAccount.accountStatuses().primary().name())
                .team(storageAccount.tags().getOrDefault(TAG_TEAM, "N/A"))
                .orgId(storageAccount.tags().getOrDefault(TAG_ORG_ID, "N/A"))
                .portalUri(buildPortalUri(storageAccount))
                .environment(SpringContext.getBean(EnvironmentService.class).getEnvironment())
                .path(path)
                .type(type)
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
        return "StorageResource{" +
                "storageAccountName='" + storageAccountName + '\'' +
                ", resourceGroup='" + resourceGroup + '\'' +
                ", connectionString='" + connectionString + '\'' +
                ", status='" + status + '\'' +
                ", team='" + team + '\'' +
                ", orgId='" + orgId + '\'' +
                ", portalUri='" + portalUri + '\'' +
                ", environment='" + environment + '\'' +
                ", path='" + path + '\'' +
                ", type=" + type +
                '}';
    }
}
