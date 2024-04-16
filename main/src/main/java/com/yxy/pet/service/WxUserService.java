package com.yxy.pet.service;

import com.yxy.pet.domain.dto.WxGetPhoneDTO;
import com.yxy.pet.domain.dto.WxUserDTO;
import com.yxy.pet.domain.vo.WxPhoneVO;
import com.yxy.pet.domain.vo.WxUserVO;

/**
 * @Desc: WxUser Service
 * @Author: yxy
 * @Time: 2022/1/12 17:10
 */
public interface WxUserService {

    /**
     * 登录
     * @param code
     * @param req
     * @return
     */
    WxUserVO login(String code, WxUserDTO req);

    /**
     * 获取手机号
     * @param req
     * @return
     */
    WxPhoneVO getPhoneNumber(WxGetPhoneDTO req);

    /**
     * 查询用户信息
     * @param openId
     * @return
     */
    WxUserVO getUserInfo(String openId);

    /**
     * 手机号码登录
     * @param phone
     * @param password
     * @return
     */
    WxUserVO phoneLogin(String phone, String password);

    Boolean updateUser(WxUserDTO req);

    WxUserVO getUserInfoById(String id);
}
