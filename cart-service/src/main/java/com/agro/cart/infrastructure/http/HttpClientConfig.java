package com.agro.cart.infrastructure.http;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({CatalogClientProperties.class, PricingClientProperties.class})
public class HttpClientConfig {
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
