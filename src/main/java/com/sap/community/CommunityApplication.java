package com.sap.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import javax.annotation.PostConstruct;

@SpringBootApplication
//@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class})
public class CommunityApplication {
//  es 7 已修复 redis 和 es 共用 netty bug
//	@PostConstruct
//	public void init() {
//		System.setProperty("es.set.netty.runtime.available.processors", "false");
//	}
	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
