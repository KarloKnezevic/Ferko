package hr.fer.zemris.ferko.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FerkoWebApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(FerkoWebApiApplication.class, args);
  }
}
