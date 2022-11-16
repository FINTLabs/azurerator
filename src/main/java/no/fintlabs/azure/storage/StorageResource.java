package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import no.fintlabs.Props;
import org.apache.commons.lang3.RandomStringUtils;

import static no.fintlabs.azure.TagNames.*;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StorageResource {

    private String storageAccountName;
    private String resourceGroup;
    private String connectionString;
    private String status;
    private String team;
    private String orgId;
    private String portalUri;
    private String environment;
    private String path;
    private StorageType type;
    private String crdName;
    private String crdNamespace;
    private String instance;
    private String partOf;


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
                .team(storageAccount.tags().getOrDefault(TAG_TEAM, TAG_DEFAULT_VALUE))
                .orgId(storageAccount.tags().getOrDefault(TAG_ORG_ID, TAG_DEFAULT_VALUE))
                .crdName(storageAccount.tags().getOrDefault(TAG_CRD_NAME, TAG_DEFAULT_VALUE))
                .crdNamespace(storageAccount.tags().getOrDefault(TAG_CRD_NAMESPACE, TAG_DEFAULT_VALUE))
                .partOf(storageAccount.tags().getOrDefault(TAG_PART_OF, TAG_DEFAULT_VALUE))
                .instance(storageAccount.tags().getOrDefault(TAG_INSTANCE, TAG_DEFAULT_VALUE))
                .portalUri(buildPortalUri(storageAccount))
                .environment(Props.getEnvironment())
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
