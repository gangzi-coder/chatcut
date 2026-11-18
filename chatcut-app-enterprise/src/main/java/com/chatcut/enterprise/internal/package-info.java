/**
 * 服务内部端点：探活、身份回显。这类接口不参与 RBAC、不写审计、不返回敏感数据；
 * 业务接口不允许放在这里。真正的健康检查走 /actuator/health。
 */
package com.chatcut.enterprise.internal;
