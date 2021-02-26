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

    Jedis jedis;


    @RequestMapping(value = "/api/list/top10", produces = { "text/html; charset=utf-8" })
    @ResponseBody
    public String getTop10() {
        return getRedisDataZrevrangeWithScores(0, 9, jedis, redisLeaderboard);
    }

    @RequestMapping(value = "/api/list/all", produces = { "text/html; charset=utf-8" })
    @ResponseBody
    public String getAll() {
        return getRedisDataZrevrangeWithScores(0, -1, jedis, redisLeaderboard);
    }

    @RequestMapping(value = "/api/list/bottom10", produces = { "text/html; charset=utf-8" })
    @ResponseBody
    public String get10() {
        return getRedisDataZrangeWithScores(0, 9, jedis, redisLeaderboard);
    }

    @RequestMapping("/api/list/inRank")
    @ResponseBody
    public String getInRank(@RequestParam(name = "start") int start,
                            @RequestParam(name = "end") int end) {
        return getRedisDataZrevrangeWithScores(start, end, jedis, redisLeaderboard);
    }

    @RequestMapping("/api/list/getBySymbol")
    @ResponseBody
    public String getBySymbol(@RequestParam(name = "symbols") List<String> symbols) {
        List<JSONObject> list = new ArrayList<>();
        for (String symbol : symbols) {
            list.add(addDataToResult(jedis.hgetAll(symbol),
                    jedis.zscore(redisLeaderboard, symbol).longValue(),
                    symbol));
        }
        return list.toString();
    }


    @RequestMapping(value = "/api/rank/update", method = RequestMethod.PATCH)
    public String updateAmount(@RequestBody Map<String, Object> payload) {
        boolean isOk = true;
        try {
            jedis.zincrby(redisLeaderboard,
                    ((Long) payload.get("amount")).doubleValue(),
                    payload.get("symbol").toString());

        }
        catch (Exception e) {
            isOk = false;
        }
        return String.format("{success: %s}", isOk);
    }

    @RequestMapping("/api/rank/reset")
    @ResponseBody
    public String reset() {
        return resetData(false, jedis, dataReadyRedisKey, redisLeaderboard);
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
                    redisLeaderboard);
        }
        catch (Exception ignored) {
        }
    }

}
