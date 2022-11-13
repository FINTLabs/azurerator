package no.fintlabs.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.storage.StorageManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfiguration {

    @Getter
    @Setter
    @Value("${fint.azure.storage-account.resource-group:rg-managed-storage}")
    private String storageAccountResourceGroup;

    @Getter
    @Setter
    @Value("${fint.azure.storage-account.polling-period-minutes:10}")
    private long storageAccountPollingPeriodInMinutes;

    @Bean
    public StorageManager createStorageMananger() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

        return StorageManager
                .authenticate(credential, profile);

    }
}
