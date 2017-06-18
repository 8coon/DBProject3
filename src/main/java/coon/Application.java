package coon;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.atomic.AtomicBoolean;


@SpringBootApplication
public class Application {


    public static AtomicBoolean triggered = new AtomicBoolean(false);


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
