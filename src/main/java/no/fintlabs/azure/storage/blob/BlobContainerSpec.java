package no.fintlabs.azure.storage.blob;

import lombok.Getter;
import lombok.Setter;
import no.fintlabs.azure.AzureSpec;


@Setter
@Getter
public class BlobContainerSpec implements AzureSpec {

    private Long lifespanDays;
}
