package org.nep.content.client;

import lombok.extern.slf4j.Slf4j;
import org.nep.common.result.Result;
import org.nep.system.entity.User;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Sentinel 熔断降级工厂
 *
 * @author NEP Team
 */
@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("用户服务调用失败，触发熔断降级: {}", cause.getMessage());

        return id -> {
            log.warn("降级处理：返回默认用户信息, userId={}", id);
            User fallbackUser = new User();
            fallbackUser.setId(id);
            fallbackUser.setRealName("用户" + id);
            fallbackUser.setPhone("00000000000");
            return Result.ok("降级数据", fallbackUser);
        };
    }
}
