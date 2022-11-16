package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy(value = false)
@Component
public class SpringContext implements ApplicationContextAware {
     
    private static ApplicationContext context;

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
     
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
         log.debug("Context {}", context.toString());
        // store ApplicationContext reference to access required beans later on
        SpringContext.context = context;
    }
}