package com.example.LibraryManagement.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    //Base64-encoded secret key
    private String key;
    private Duration duration = Duration.ofHours(1);
}
