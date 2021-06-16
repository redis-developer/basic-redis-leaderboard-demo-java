# Basic Redis Leaderboard Demo Java (Spring)

A basic leaderboard application using Redis and Java/Spring. The application models public companies and their market capitalization.

![alt text](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/screenshot001.png)

# Overview video

Here's a short video that explains the project and how it uses Redis:

[![Watch the video on YouTube](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/YTThumbnail.png)](https://www.youtube.com/watch?v=zzinHxdZ34I)

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

1. Open link google cloud console.

![1 step](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/1.png)

2. Click "Edit and deploy new revision" button.

![2 step](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/2.png)

3. Add environment.

![3 step](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/3.png)

4.  Select vpc-connector and deploy application.

![4  step](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/4.png)

<a href="https://github.com/GoogleCloudPlatform/cloud-run-button/issues/108#issuecomment-554572173">
Problem with unsupported flags when deploying google cloud run button
</a>

## How it works?

![How it works](https://github.com/redis-developer/basic-redis-leaderboard-demo-java/raw/master/docs/screenshot001.png)


# How it works?
## 1. How the data is stored:
<ol>
    <li>Apple's market cap is modeled as follows:
      <pre> <a href="https://redis.io/commands/hset">HSET</a> "company:AAPL" symbol "AAPL" market_cap "2600000000000" country USA</pre>
     </li>
    <li>A company's rank is stored using a <a href="https://redislabs.com/ebook/part-1-getting-started/chapter-1-getting-to-know-redis/1-2-what-redis-data-structures-look-like/1-2-5-sorted-sets-in-redis/">sorted set</a>. 
      <pre><a href="https://redis.io/commands/zadd">ZADD</a>  companyLeaderboard 2600000000000 company:AAPL</pre>
    </li>
</ol>

<br/>

## 2. How the data is accessed:
<ol>
    <li>Top 10 companies: <pre><a href="https://redis.io/commands/zrevrange">ZREVRANGE</a> companyLeaderboard 0 9 WITHSCORES</pre> </li>
    <li>All companies: <pre><a href="https://redis.io/commands/zrevrange">ZREVRANGE</a> companyLeaderboard 0 -1 WITHSCORES</pre> </li>
    <li>Bottom 10 companies: <pre><a href="https://redis.io/commands/zrange">ZRANGE</a> companyLeaderboard 0 9 WITHSCORES</pre></li>
    <li>Between rank 10 and 15: <pre><a href="https://redis.io/commands/zrevrange">ZREVRANGE</a> companyLeaderboard 9 14 WITHSCORES</pre></li>
    <li>Show ranks of AAPL, FB and TSLA: <pre><a href="https://redis.io/commands/zrevrange">ZREVRANGE</a>  companyLeaderBoard company:AAPL company:FB company:TSLA</pre> </li>
    <!-- <li>Pagination: Show 1st 10 companies: <pre><a href="https://redis.io/commands/zscan">ZSCAN</a> 0 companyLeaderBoard COUNT 10 7.Pagination: Show next 10 companies: ZSCAN &lt;return value from the 1st 10 companies&gt; companyLeaderBoard COUNT 10 </li> -->
    <li>Adding 1 billion to market cap of FB company: <pre><a href="https://redis.io/commands/zincrby">ZINCRBY</a> companyLeaderBoard 1000000000 "company:FB"</pre></li>
    <li>Reducing 1 billion of market cap of FB company: <pre><a href="https://redis.io/commands/zincrby">ZINCRBY</a> companyLeaderBoard -1000000000 "company:FB"</pre></li>
    <li>Companies between 500 billion and 1 trillion: <pre><a href="https://redis.io/commands/zcount">ZCOUNT</a> companyLeaderBoard 500000000000 1000000000000</pre></li>
    <li>Companies over a Trillion: <pre><a href="https://redis.io/commands/zcount">ZCOUNT</a> companyLeaderBoard 1000000000000 +inf</pre> </li>
</ol>


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
