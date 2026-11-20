package com.chatcut.admin.system.model.resp;

import cn.hutool.core.lang.tree.Tree;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * MenuTreeSelect响应数据。
 */
@Data
@AllArgsConstructor
public class MenuTreeSelectResp {

    /**
     * checkedKeys.
     */
    private List<Long> checkedKeys;

    /**
     * menus.
     */
    private List<Tree<Long>> menus;

}
