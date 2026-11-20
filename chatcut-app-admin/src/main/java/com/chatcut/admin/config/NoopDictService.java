package com.chatcut.admin.config;

import com.chatcut.common.core.domain.dto.DictDataDTO;
import com.chatcut.common.core.domain.dto.DictTypeDTO;
import com.chatcut.common.core.service.DictService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 字典服务空实现。
 *
 * <p>管理端已裁剪字典模块（{@code sys_dict_type} / {@code sys_dict_data} 表不保留），
 * 但 ruoyi-common-translation 的翻译器仍依赖 {@link DictService} 注入，
 * 此处提供恒空实现作为兜底：字典翻译一律返回空值，不影响其他翻译类型。</p>
 *
 * @author chatcut
 */
@Component
public class NoopDictService implements DictService {

    /**
     * 根据字典类型查询字典数据列表。
     *
     * @param dictType 字典类型
     * @return 恒为空列表（字典模块已裁剪）
     */
    @Override
    public List<DictDataDTO> getDictData(String dictType) {
        return List.of();
    }

    /**
     * 根据字典类型查询字典类型信息。
     *
     * @param dictType 字典类型
     * @return 恒为 {@code null}（字典模块已裁剪）
     */
    @Override
    public DictTypeDTO getDictType(String dictType) {
        return null;
    }

    /**
     * 根据字典类型查询全部字典数据（label -> value 映射）。
     *
     * @param dictType 字典类型
     * @return 恒为空映射（字典模块已裁剪）
     */
    @Override
    public Map<String, String> getAllDictByDictType(String dictType) {
        return Map.of();
    }

    /**
     * 根据租户、字典类型与字典值翻译字典标签。
     *
     * @param tenantId  租户编号（忽略）
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @return 恒为空字符串（字典模块已裁剪）
     */
    @Override
    public String getDictLabel(String tenantId, String dictType, String dictValue) {
        return "";
    }

    /**
     * 根据租户、字典类型与字典标签反查字典值。
     *
     * @param tenantId  租户编号（忽略）
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 恒为空字符串（字典模块已裁剪）
     */
    @Override
    public String getDictValue(String tenantId, String dictType, String dictLabel) {
        return "";
    }
}
