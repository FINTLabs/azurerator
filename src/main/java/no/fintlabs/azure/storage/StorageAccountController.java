package no.fintlabs.azure.storage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("api/flais/operators/azurerator")
public class StorageAccountController {

    private final StorageAccountRepository storageAccountRepository;

    public StorageAccountController(StorageAccountRepository storageAccountRepository) {
        this.storageAccountRepository = storageAccountRepository;
    }

    @GetMapping("storage-accounts")
    public Mono<Map<String, String>> getStorageAccounts() {
        return Mono.justOrEmpty(storageAccountRepository.getAll());
    }
}
