package io.github.berrachdis.feignwrapper;

import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = FeignWrapperProperties.class)
public class FeignWrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeignWrapperApplication.class, args);
	}

}
