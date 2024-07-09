package com.viettel.importwiz.config;

import com.viettel.cn.PassTranformerCN;
import com.viettel.security.PassTranformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@Profile("prod")
public class RedisConfigProd {

    @Value("${spring.redis.sentinel.master}")
    private String sentinelMaster;

    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${spring.redis.sentinel.password}")
    private String sentinelPassword;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    @Profile("prod")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master(sentinelMaster);
        Arrays.stream(sentinelNodes.split(",")).collect(Collectors.toList()).forEach(
            node -> {
                if (node.split(":").length == 2) {
                    String host = node.split(":")[0];
                    int port = Integer.parseInt(node.split(":")[1]);
                    RedisNode redisNode = new RedisNode(host, port);
                    sentinelConfig.addSentinel(redisNode);
                }
            }
        );
        if (!sentinelConfig.getSentinels().isEmpty()) {
            sentinelConfig.setSentinelPassword(PassTranformerCN.decrypt(sentinelPassword));
            try {
                PassTranformer.setInputKey(sentinelMaster);
                sentinelConfig.setPassword(PassTranformer.decrypt(password));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new LettuceConnectionFactory(sentinelConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}
