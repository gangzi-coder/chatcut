package com.chatcut.admin.system.model.resp;

import cn.hutool.core.lang.tree.Tree;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DeptTreeSelect响应数据。
 */
@Data
@AllArgsConstructor
public class DeptTreeSelectResp {

    /**
     * checkedKeys.
     */
    private List<Long> checkedKeys;

    /**
     * depts.
     */
    private List<Tree<Long>> depts;

}
