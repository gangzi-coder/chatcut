package com.chatcut.common.mybatis.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有表的公共字段，业务 Entity 继承它即可。
 *
 * <p>字段与数据库列一一对应，新增公共列时在此扩展并同步所有建表脚本。</p>
 */
@Data
public abstract class BaseEntity implements Serializable {

    /** 主键，雪花 id（数据库层不使用自增）。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间，插入时由 AutoFillHandler 自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入/更新时由 AutoFillHandler 自动填充。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0=未删除 1=已删除（MyBatis-Plus 全局配置，查询自动过滤）。 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 乐观锁版本号，更新时自动 +1，并发修改冲突时更新失败。 */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
