package com.chatcut.admin.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.Constants;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.ServletUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.core.utils.ip.AddressUtils;
import com.chatcut.common.log.event.LogininforEvent;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.admin.system.repository.entity.SysLogininforEntity;
import com.chatcut.admin.system.model.req.SysLogininforReq;
import com.chatcut.admin.system.model.resp.SysClientResp;
import com.chatcut.admin.system.model.resp.SysLogininforResp;
import com.chatcut.admin.system.repository.mapper.SysLogininforMapper;
import com.chatcut.admin.system.service.ISysClientService;
import com.chatcut.admin.system.service.ISysLogininforService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统访问日志情况信息 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLogininforServiceImpl implements ISysLogininforService {

    private final SysLogininforMapper baseMapper;

    private final ISysClientService clientService;

    /**
     * 记录登录信息
     *
     * @param logininforEvent 登录事件
     */
    @Async
    @EventListener
    public void recordLogininfor(LogininforEvent logininforEvent) {
        HttpServletRequest request = logininforEvent.getRequest();
        final UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
        final String ip = ServletUtils.getClientIP(request);
        // 客户端信息
        String clientId = request.getHeader(LoginHelper.CLIENT_KEY);
        SysClientResp client = null;
        if (StringUtils.isNotBlank(clientId)) {
            client = clientService.queryByClientId(clientId);
        }

        String address = AddressUtils.getRealAddressByIP(ip);
        StringBuilder s = new StringBuilder();
        s.append(getBlock(ip));
        s.append(address);
        s.append(getBlock(logininforEvent.getUsername()));
        s.append(getBlock(logininforEvent.getStatus()));
        s.append(getBlock(logininforEvent.getMessage()));
        // 打印信息到日志
        log.info(s.toString(), logininforEvent.getArgs());
        // 获取客户端操作系统
        String os = userAgent.getOs().getName();
        // 获取客户端浏览器
        String browser = userAgent.getBrowser().getName();
        // 封装对象
        SysLogininforReq logininfor = new SysLogininforReq();
        logininfor.setUserName(logininforEvent.getUsername());
        if (ObjectUtil.isNotNull(client)) {
            logininfor.setClientKey(client.getClientKey());
            logininfor.setDeviceType(client.getDeviceType());
        }
        logininfor.setIpaddr(ip);
        logininfor.setLoginLocation(address);
        logininfor.setBrowser(browser);
        logininfor.setOs(os);
        logininfor.setMsg(logininforEvent.getMessage());
        // 日志状态
        if (StringUtils.equalsAny(logininforEvent.getStatus(), Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
            logininfor.setStatus(Constants.SUCCESS);
        } else if (Constants.LOGIN_FAIL.equals(logininforEvent.getStatus())) {
            logininfor.setStatus(Constants.FAIL);
        }
        // 插入数据
        insertLogininfor(logininfor);
    }

    private String getBlock(Object msg) {
        if (msg == null) {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }

    /**
     * 分页查询登录日志列表
     *
     * @param logininfor 查询条件
     * @param pageQuery  分页参数
     * @return 登录日志分页列表
     */
    @Override
    public TableDataInfo<SysLogininforResp> selectPageLogininforList(SysLogininforReq logininfor, PageQuery pageQuery) {
        Map<String, Object> params = logininfor.getParams();
        LambdaQueryWrapper<SysLogininforEntity> lqw = new LambdaQueryWrapper<SysLogininforEntity>()
            .like(StringUtils.isNotBlank(logininfor.getIpaddr()), SysLogininforEntity::getIpaddr, logininfor.getIpaddr())
            .eq(StringUtils.isNotBlank(logininfor.getStatus()), SysLogininforEntity::getStatus, logininfor.getStatus())
            .like(StringUtils.isNotBlank(logininfor.getUserName()), SysLogininforEntity::getUserName, logininfor.getUserName())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysLogininforEntity::getLoginTime, params.get("beginTime"), params.get("endTime"));
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderByDesc(SysLogininforEntity::getInfoId);
        }
        Page<SysLogininforResp> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 新增系统登录日志
     *
     * @param bo 访问日志对象
     */
    @Override
    public void insertLogininfor(SysLogininforReq bo) {
        SysLogininforEntity logininfor = MapstructUtils.convert(bo, SysLogininforEntity.class);
        logininfor.setLoginTime(new Date());
        baseMapper.insert(logininfor);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininforResp> selectLogininforList(SysLogininforReq logininfor) {
        Map<String, Object> params = logininfor.getParams();
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysLogininforEntity>()
            .like(StringUtils.isNotBlank(logininfor.getIpaddr()), SysLogininforEntity::getIpaddr, logininfor.getIpaddr())
            .eq(StringUtils.isNotBlank(logininfor.getStatus()), SysLogininforEntity::getStatus, logininfor.getStatus())
            .like(StringUtils.isNotBlank(logininfor.getUserName()), SysLogininforEntity::getUserName, logininfor.getUserName())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysLogininforEntity::getLoginTime, params.get("beginTime"), params.get("endTime"))
            .orderByDesc(SysLogininforEntity::getInfoId));
    }

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds) {
        return baseMapper.deleteByIds(Arrays.asList(infoIds));
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLogininfor() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
