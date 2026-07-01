package org.nep.system.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户注册事件监听器 - 异步发送欢迎邮件
 * EmailService 由 nep-framework 或 nep-system 模块提供
 */
@Component
public class UserRegisterListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisterListener.class);

    @Async("taskExecutor")
    @EventListener
    public void handleRegisterEvent(UserRegisterEvent event) {
        log.info("收到用户注册事件: phone={}, name={}", event.getUser().getPhone(), event.getUser().getRealName());
        // 邮件发送由 nep-system 模块的 EmailService 统一处理
        // 此处仅记录日志，避免模块间循环依赖
    }
}
