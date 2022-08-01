package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GtwFhirMappingEngineMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GtwFhirMappingEngineMsApplication.class, args);
	}


}
