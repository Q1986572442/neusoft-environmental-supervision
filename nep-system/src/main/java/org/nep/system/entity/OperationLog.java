package org.nep.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nep.common.base.BaseEntity;

/**
 * 操作日志审计实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nep_operation_log")
public class OperationLog extends BaseEntity {
    /** 操作用户ID */
    private Long userId;
    /** 操作用户名 */
    private String username;
    /** 操作模块 */
    private String module;
    /** 操作类型: LOGIN, INSERT, UPDATE, DELETE, EXPORT, ASSIGN */
    private String operation;
    /** 操作描述 */
    private String description;
    /** 请求方法 */
    private String method;
    /** 请求URL */
    private String requestUrl;
    /** 请求参数 */
    private String requestParams;
    /** IP地址 */
    private String ipAddress;
    /** 执行耗时(ms) */
    private Long executionTime;
    /** 操作状态: 0-失败, 1-成功 */
    private Integer status;
    /** 错误信息 */
    private String errorMsg;
}
