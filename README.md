## Demo implementation of an API rate limiter with Spring Boot and Redis

In this demo, I implemented rate limiter with [sliding window](https://konghq.com/blog/how-to-design-a-scalable-rate-limiting-algorithm/)
algorithm. 


**Requirement:**

* Redis
* Java 8

For Redis client, I used [Lettuce](https://github.com/lettuce-io/lettuce-core), which is shipped by default with Spring.


**How to run:**
- Run a local Redis server. I used the Redis image from [Docker](https://hub.docker.com/_/redis).
- Then just run ./gradlew bootRun