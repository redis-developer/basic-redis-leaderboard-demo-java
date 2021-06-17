# Basic Redis Leaderboard Demo Java (Spring)

A basic leaderboard application using Redis and Java/Spring. The application models public companies and their market capitalization.

## Try it out

<p>
    <a href="https://heroku.com/deploy" target="_blank">
        <img src="https://www.herokucdn.com/deploy/button.svg" alt="Deploy to Heroku" width="200px"/>
    <a>
</p>
<p>
    <a href="https://deploy.cloud.run?dir=server" target="_blank">
        <img src="https://deploy.cloud.run/button.svg" alt="Run on Google Cloud" width="200px"/>
    </a>
    (See notes: How to run on Google Cloud)
</p>

## How to run on Google Cloud

<p>
    If you don't have redis yet, plug it in  (https://spring-gcp.saturnism.me/app-dev/cloud-services/cache/memorystore-redis).
    After successful deployment, you need to manually enable the vpc connector as shown in the pictures:
</p>

1. Click on "Run on Google Cloud"

![1 step](https://raw.githubusercontent.com/redis-developer/basic-redis-leaderboard-demo-java/master/1.jpg)

2. Click "Edit and deploy new revision" button and choose "Variables and Secrets".

![2 step](https://raw.githubusercontent.com/redis-developer/basic-redis-leaderboard-demo-java/master/2.jpg)

3. Manage the traffic


![3  step](https://raw.githubusercontent.com/redis-developer/basic-redis-leaderboard-demo-java/master/docs/3.jpg)

<a href="https://github.com/GoogleCloudPlatform/cloud-run-button/issues/108#issuecomment-554572173">
Problem with unsupported flags when deploying google cloud run button
</a>

## How it works?


## 1. How the data is stored:

####  How the data is stored:

- The AAPL's details - market cap of 2.6 triillions and USA origin - are stored in a hash like below:
  ```bash
   HSET "company:AAPL" symbol "AAPL" market_cap "2600000000000" country USA
  ```

- The Ranks of AAPL of 2.6 trillions are stored in a ZSET.

   ```bash
    ZADD  companyLeaderboard 2600000000000 company:AAPL
   ```

####  How the data is accessed:

- Top 10 companies:

  ```bash
   ZREVRANGE companyLeaderboard 0 9 WITHSCORES
  ```

- All companies:

  ```bash
   ZREVRANGE companyLeaderboard 0 -1 WITHSCORES
  ```

- Bottom 10 companies:

  ```bash
   ZRANGE companyLeaderboard 0 9 WITHSCORES
  ```

- Between rank 10 and 15:

  ```bash
   ZREVRANGE companyLeaderboard 9 14 WITHSCORES
  ```

- Show ranks of AAPL, FB and TSLA:

  ```bash
   ZREVRANGE  companyLeaderBoard company:AAPL company:FB company:TSLA
  ```

- Adding 1 billion to market cap of FB company:

  ```bash
   ZINCRBY companyLeaderBoard 1000000000 "company:FB"
  ```

- Reducing 1 billion of market cap of FB company:

  ```bash
   ZINCRBY companyLeaderBoard -1000000000 "company:FB"
  ```

- Companies between 500 billion and 1 trillion:

  ```bash
   ZCOUNT companyLeaderBoard 500000000000 1000000000000
  ```

- Companies over a Trillion:

   ```bash
    ZCOUNT companyLeaderBoard 1000000000000 +inf
   ```


## How to run it locally?

#### Open the files server/.env.example to see the available environment variables. You may set these variables when you start the application.
   	- REDIS_URL: Redis server url
    - REDIS_HOST: Redis server host
	- REDIS_PORT: Redis server port
	- REDIS_PASSWORD: Redis server password

#### Run backend

1. Install gradle (Use Gradle 6.3 or later) (on mac: https://gradle.org/install/) 

2. Install JDK (use 8 or later version) (on mac: https://docs.oracle.com/javase/10/install/installation-jdk-and-jre-macos.htm)

3. Set any relevant environment variables (if not connecting to Redis on localhost:6379). For example:

``` sh
$ REDIS_PORT=6379
```

3. From the root directory of the project, run the following commands:
``` sh
cd server
./gradlew build
./gradlew run
```

4. Point your browser to `localhost:5000`.
