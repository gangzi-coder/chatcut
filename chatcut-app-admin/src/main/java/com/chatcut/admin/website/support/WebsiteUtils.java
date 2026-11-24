package com.chatcut.admin.website.support;

import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.StringUtils;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 官网域小工具：ID 解析、CSV 明文导出与线索编号生成。
 */
public final class WebsiteUtils {

    private static final DateTimeFormatter LEAD_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private WebsiteUtils() {
    }

    /**
     * 前端 ID 统一走 String（规避 JS 长整型精度丢失），此处转回 Long；空值返回 null。
     */
    public static Long parseIdNullable(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("非法 ID：" + id);
        }
    }

    /**
     * 必填 ID 解析，空或非法直接拒绝。
     */
    public static Long parseId(String id) {
        Long parsed = parseIdNullable(id);
        if (parsed == null) {
            throw new ServiceException("主键不能为空");
        }
        return parsed;
    }

    /**
     * 生成线索编号：LD + 时间戳 + 4 位随机，唯一键兜底。
     */
    public static String nextLeadNo() {
        return "LD" + LocalDateTime.now().format(LEAD_NO_FORMAT)
            + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /**
     * 同步导出 CSV（UTF-8 带 BOM，Excel 直接打开中文不乱码）。字段按明文输出，不脱敏。
     *
     * @param response HTTP 响应
     * @param filename 文件名（自动加 .csv 后缀）
     * @param rows     数据行；首行为表头，调用方负责
     */
    public static void writeCsv(HttpServletResponse response, String filename, List<String[]> rows) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(escape(row[i]));
            }
            sb.append('\n');
        }
        try (OutputStream out = response.getOutputStream()) {
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\r", "").replace("\n", " ");
        if (v.contains(",") || v.contains("\"")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }
}
