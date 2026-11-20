package com.chatcut.admin.system.model.resp;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * CacheListInfo响应数据。
 */
@Data
@AllArgsConstructor
public class CacheListInfoResp {

    /**
     * info.
     */
    private Properties info;

    /**
     * dbSize.
     */
    private Long dbSize;

    /**
     * commandStats.
     */
    private List<Map<String, String>> commandStats;

}
