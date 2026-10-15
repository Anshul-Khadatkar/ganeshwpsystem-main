package com.example.ganpativargani;

import com.example.ganpativargani.config.DebugProperties;
import com.example.ganpativargani.whatsapp.WhatsAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({WhatsAppProperties.class, DebugProperties.class})
public class GanpatiVarganiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GanpatiVarganiApplication.class, args);
    }
}
