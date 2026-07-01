package org.nep.content.client;

import org.nep.common.result.Result;
import org.nep.system.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign 远程调用客户端 - 调用用户微服务
 * 用于评论/点赞等社交功能中的用户信息获取
 *
 * @author NEP Team
 */
@FeignClient(
    name = "nep-service-user",
    path = "/api/user",
    fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {

    @GetMapping("/{id}")
    Result<User> getById(@PathVariable("id") Long id);
}
