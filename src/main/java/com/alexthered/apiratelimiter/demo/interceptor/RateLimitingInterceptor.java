package com.alexthered.apiratelimiter.demo.interceptor;

import com.alexthered.apiratelimiter.demo.interceptor.exception.RateLimitExceededException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RateLimitingInterceptor extends OncePerRequestFilter  {

  @Value("${rate.limiter.max.request}")
  private Integer maxRequestPerMinute;

  private static final Integer DEFAULT_REDIS_EXPIRATION_DURATION = 5; //minutes

  @Autowired
  private RedisTemplate<String, Long> redisTemplate;


  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return false; //filter everything
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // get IP from request
    String ip = request.getRemoteAddr();

    log.debug(String.format("Getting request from ip: %s", ip));

    try {
      validateRateLimitForIp(ip);
    } catch (RateLimitExceededException e) {
      // return rate limit exceed response
      response.sendError(429, "Slow down...");
    }


    filterChain.doFilter(request, response);
  }

  // check if a request from an IP has exceeded the threshold
  // by calculating the a weighted average of number of request in current minute
  // and last minute
  // the weight is determined by the elapsed seconds in current minute
  private void validateRateLimitForIp(String ip) throws RateLimitExceededException {

    LocalDateTime now = LocalDateTime.now();
    Integer elapsedSeconds = now.getSecond();
    Integer currentMinute = now.getMinute();

    // get modulo 60 so at 0 minute we get 59 (from the previous hour)
    Integer lastMinute = (currentMinute + 59 ) % 60;

    Long requestCountLastMinute = redisTemplate.opsForValue().get(constructRedisKey(ip, lastMinute));

    if (requestCountLastMinute == null) {
      requestCountLastMinute = 0L;
    }

    // increase the count for current minute
    Long requestCountCurrentMinute = redisTemplate.opsForValue().increment(constructRedisKey(ip, currentMinute));
    // set expiration for the key as Redis does not support setting timeout together with increment operation
    redisTemplate.expire(constructRedisKey(ip, currentMinute), DEFAULT_REDIS_EXPIRATION_DURATION, TimeUnit.MINUTES);

    // calculate weighted average
    Double weightedAvg = ((double) elapsedSeconds * requestCountCurrentMinute.doubleValue()
        + (double) (59 - elapsedSeconds) * requestCountLastMinute.doubleValue()) / 59.0;

    if (weightedAvg.intValue() >= DEFAULT_REDIS_EXPIRATION_DURATION) {
      throw new RateLimitExceededException();
    }
  }

  private String constructRedisKey(String ip, Integer minute) {
    return String.format("count#%s#%d", ip, minute);
  }

}
