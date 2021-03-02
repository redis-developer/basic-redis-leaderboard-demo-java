package com.redis.basicredisleaderboarddemojava.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.*;

import static com.redis.basicredisleaderboarddemojava.controller.Utils.*;


@RestController
@Service
@Component
public class Api implements ApplicationListener<ContextRefreshedEvent> {
    @Value("${REDIS_URL}")
    private String redisUrl;

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private String redisPort;

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;

    @Value("${REDIS_DB}")
    private String redisDB;

    @Value("${REDIS_LEADERBOARD}")
    private String redisLeaderboard;

    @Value("${LEADERBOARD_DATA_READY}")
    private String dataReadyRedisKey;

    @Value("${KEY_PREFIX}")
    private String keyPrefix;

    Jedis jedis;


    @RequestMapping(value = "/api/list/top10", produces = { "text/html; charset=utf-8" })
    @ResponseBody
    public String getTop10() {
        return getRedisDataZrevrangeWithScores(0, 9, jedis, redisLeaderboard, keyPrefix);
    }

    @RequestMapping(value = "/api/list/all", produces = { "text/html; charset=utf-8" })
    @ResponseBody
    public String getAll() {
        return getRedisDataZrevrangeWithScores(0, -1, jedis, redisLeaderboard, keyPrefix);
    }

    @RequestMapping(value = "/api/list/bottom10", produces = { "text/html; charset=utf-8" })
    @ResponseBody
    public String get10() {
        return getRedisDataZrangeWithScores(0, 9, jedis, redisLeaderboard, keyPrefix);
    }

    @RequestMapping("/api/list/inRank")
    @ResponseBody
    public String getInRank(@RequestParam(name = "start") int start,
                            @RequestParam(name = "end") int end) {
        return getRedisDataZrevrangeWithScores(start, end, jedis, redisLeaderboard, keyPrefix);
    }

    @RequestMapping("/api/list/getBySymbol")
    @ResponseBody
    public String getBySymbol(@RequestParam(name = "symbols") List<String> symbols) {
        List<JSONObject> list = new ArrayList<>();
        String updateSymbol;
        for (String symbol : symbols) {
            updateSymbol = addPrefix(keyPrefix, symbol);
            list.add(addDataToResult(jedis.hgetAll(updateSymbol),
                    jedis.zscore(redisLeaderboard, updateSymbol).longValue(),
                    symbol, keyPrefix));
        }
        return list.toString();
    }


    @RequestMapping(value = "/api/rank/update")
    public String updateAmount(@RequestParam(name = "symbol") String symbol,
                               @RequestParam(name = "amount") Long amount) {
        boolean isOk = true;
        try {
            jedis.zincrby(redisLeaderboard,
                    (amount).doubleValue(),
                    addPrefix(keyPrefix, symbol));

        }
        catch (Exception e) {
            isOk = false;
        }
        return String.format("{success: %s}", isOk);
    }

    @RequestMapping("/api/rank/reset")
    @ResponseBody
    public String reset() {
        return resetData(false, jedis, dataReadyRedisKey, redisLeaderboard, keyPrefix);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            if (!redisUrl.equals("")) {
                jedis = new Jedis(redisUrl);
            } else {
                jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
            }
            if (!redisPassword.equals("")){
                jedis.auth(redisPassword);
            }
            if (!redisDB.equals("")){
                jedis.select(Integer.parseInt(redisDB));
            }
            resetData(Boolean.parseBoolean(
                    jedis.get(dataReadyRedisKey)),
                    jedis, dataReadyRedisKey,
                    redisLeaderboard, keyPrefix);
        }
        catch (Exception ignored) {
        }
    }

}
