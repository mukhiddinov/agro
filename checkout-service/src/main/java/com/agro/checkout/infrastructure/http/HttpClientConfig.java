package com.agro.checkout.infrastructure.http;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({CartClientProperties.class, CatalogClientProperties.class,
    PricingClientProperties.class, ShippingClientProperties.class, UserClientProperties.class,
    OrderClientProperties.class})
public class HttpClientConfig {
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
