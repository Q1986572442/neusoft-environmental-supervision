package org.nep.feedback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 环保反馈微服务 - 启动类
 * 负责公众监督反馈、AQI检测、网格指派、邮件通知
 *
 * @author NEP Team
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("org.nep.system.mapper")
@ComponentScan(basePackages = {"org.nep.feedback", "org.nep.system", "org.nep.framework", "org.nep.common"})
public class FeedbackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedbackServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("  NEP Feedback Service 启动成功!");
        System.out.println("  端口: 8083");
        System.out.println("  注册中心: Nacos (localhost:8848)");
        System.out.println("========================================");
    }
}
