package no.fintlabs;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class EnvironmentService {

    private final KubernetesClient kubernetesClient;

    @Getter
    private static String environment;



    public EnvironmentService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @PostConstruct
    public void init() {

        environment = kubernetesClient.getConfiguration().getCurrentContext().getName();
    }
}
