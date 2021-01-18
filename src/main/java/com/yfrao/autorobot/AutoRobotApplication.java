package com.yfrao.autorobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class AutoRobotApplication {
    private static final Logger logger = LoggerFactory.getLogger(AutoRobotApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(AutoRobotApplication.class, args);
        logger.debug("--Application Started--");
    }
}