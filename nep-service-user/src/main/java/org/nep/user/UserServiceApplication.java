package org.nep.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 用户认证微服务 - 启动类
 * 负责用户注册、登录、JWT鉴权、区域管理
 *
 * @author NEP Team
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@MapperScan("org.nep.system.mapper")
@ComponentScan(basePackages = {"org.nep.user", "org.nep.system", "org.nep.framework", "org.nep.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("  NEP User Service 启动成功!");
        System.out.println("  端口: 8082");
        System.out.println("  注册中心: Nacos (localhost:8848)");
        System.out.println("========================================");
    }
}
