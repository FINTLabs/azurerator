package no.fintlabs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class Props {

    @Value("${fint.operator.environment}")
    private String operatorEnvironment;

    @Getter
    private static String environment;

    @PostConstruct
    public void init() {
        environment = operatorEnvironment;
    }

}
