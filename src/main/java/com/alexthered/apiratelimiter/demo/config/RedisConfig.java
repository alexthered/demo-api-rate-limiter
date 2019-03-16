package com.alexthered.apiratelimiter.demo.config;


import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;


@Slf4j
@Configuration
public class RedisConfig {

  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private Integer redisPort;


  @PostConstruct
  public void init() {
    RedisConnectionFactory connectionFactory = redisConnectionFactory();
    //check if the connection to Redis server is setup properly
    if (connectionFactory == null
        || connectionFactory.getConnection() == null
        || connectionFactory.getConnection().ping() == null) {
      log.error("Redis server is not available");
    }

    log.info(String.format("Redis server is enabled at %s:%d", redisHost, redisPort));
  }

  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {

    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
    return new LettuceConnectionFactory(configuration);
  }

}
