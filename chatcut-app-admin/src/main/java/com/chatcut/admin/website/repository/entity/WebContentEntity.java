package com.chatcut.admin.website.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 官网内容主表 web_content。
 *
 * <p>刻意不继承 BaseEntity：官网表时间列命名为 created_at/updated_at，与若依
 * create_time/update_time 不一致，自持字段避免映射错位。</p>
 */
@Data
@TableName("web_content")
public class WebContentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 内容主键，雪花 ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 内容类型：PAGE/NOTICE/CASE/RESOURCE/POLICY/PRODUCT，已有内容固定不可修改。 */
    private String contentType;

    /** 内容分组：首页/企业服务/资源与支持/法律与信任，可扩展字典。 */
    private String groupKey;

    /** 当前展示标题（最近一次发布或草稿保存的标题）。 */
    private String title;

    /** 内容状态：DRAFT/PUBLISHED/OFFLINE。 */
    private String status;

    /** 当前正式发布版本号，未发布为 null。 */
    private Integer publishedVersion;

    /** 创建管理员 ID。 */
    private Long createdBy;

    /** 最后更新管理员 ID。 */
    private Long updatedBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    /** 删除标志：0正常，1已删除（全局逻辑删除字段）。 */
    private String delFlag;

    /** 乐观锁版本号。 */
    @Version
    private Integer version;
}
