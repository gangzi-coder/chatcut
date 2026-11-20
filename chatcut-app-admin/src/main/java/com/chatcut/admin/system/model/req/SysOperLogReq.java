package com.chatcut.admin.system.model.req;

import com.chatcut.common.log.event.OperLogEvent;
import com.chatcut.admin.system.repository.entity.SysOperLogEntity;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志记录业务对象 sys_oper_log
 *
 * @author Michelle.Chung
 * @date 2023-02-07
 */

@Data
@AutoMappers({
    @AutoMapper(target = SysOperLogEntity.class, reverseConvertGenerate = false),
    @AutoMapper(target = OperLogEvent.class)
})
public class SysOperLogReq {

    /**
     * 日志主键
     */
    private Long operId;

    /**
     * 租户编号
     */
    private String tenantId;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private Integer businessType;

    /**
     * 业务类型数组
     */
    private Integer[] businessTypes;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    private Integer operatorType;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请求URL
     */
    private String operUrl;

    /**
     * 主机地址
     */
    private String operIp;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     * @see com.chatcut.common.log.enums.BusinessStatus 状态取值
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private Date operTime;

    /**
     * 消耗时间
     */
    private Long costTime;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
