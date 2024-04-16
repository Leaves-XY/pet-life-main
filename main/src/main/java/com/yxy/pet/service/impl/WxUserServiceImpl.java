package com.yxy.pet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yxy.pet.common.basic.utils.HttpRequest;
import com.yxy.pet.config.properties.WechatAppletProperties;
import com.yxy.pet.domain.dto.WxGetPhoneDTO;
import com.yxy.pet.domain.dto.WxUserDTO;
import com.yxy.pet.domain.entity.WxUser;
import com.yxy.pet.domain.vo.WxPhoneVO;
import com.yxy.pet.domain.vo.WxUserVO;
import com.yxy.pet.factory.ResponseBeanFactory;
import com.yxy.pet.mapper.WxUserMapper;
import com.yxy.pet.service.WxUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Desc:
 * @Author: yxy
 * @Time: 2022/1/13 14:13
 */
@Slf4j
@AllArgsConstructor
@Service
public class WxUserServiceImpl implements WxUserService {
    private final WechatAppletProperties wechatAppletProperties;
    private final WxUserMapper wxUserMapper;

    @Override
    public WxUserVO login(String code, WxUserDTO req) {
        String appId = wechatAppletProperties.getAppId();
        String secret = wechatAppletProperties.getAppSecret();
        String url = wechatAppletProperties.getAccessTokenApiUrl();
        // 参数
        String param = "appid=" + appId +
                "&secret=" + secret +
                "&js_code=" + code +
                "&grant_type=authorization_code";
        // 发送请求
        String response = HttpRequest.sendPost(url, param);
        log.info("微信返回的结果：{}", response);
        Map responseMap = JSONObject.parseObject(response, HashMap.class);

        String openId = String.valueOf(responseMap.get("openid"));
        if (ObjectUtils.isEmpty(responseMap.get("openid"))) {
            log.error("微信接口调用失败, resp: {}", response);
            return null;
        }
        String sessionKey = String.valueOf(responseMap.get("session_key"));
        String unionId = String.valueOf(responseMap.get("unionid"));
        log.info("response：{}", responseMap);
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, openId));
        // 已注册 返回用户信息
        if (!ObjectUtils.isEmpty(wxUser)) {
            WxUserDTO wxUserDTO = BeanUtil.copyProperties(wxUser, WxUserDTO.class);
            wxUserDTO.setNickname(wxUser.getNickName());
            wxUser = ResponseBeanFactory.getWxUser(openId, sessionKey, unionId, wxUserDTO);
//            wxUserMapper.update(wxUser, Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, openId));
            log.info("执行成功[用户登录]");
            return WxUserVO.fromWxUser(wxUser);
        }
        // 未注册
        wxUser = ResponseBeanFactory.getWxUser(openId, sessionKey, unionId, req);
        wxUserMapper.insert(wxUser);
        log.info("执行成功[用户首次授权]");
        return WxUserVO.fromWxUser(wxUser);
    }

    @Override
    public WxPhoneVO getPhoneNumber(WxGetPhoneDTO req) {
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getSessionKey, req.getSessionKey()));
        String phoneNumber = getPhone(wxUser.getSessionKey(), req);
        wxUser.setPhone(phoneNumber);
        // 保存手机号
        wxUserMapper.updateById(wxUser);
        log.info("执行成功[获取用户手机号]");
        return WxPhoneVO.builder()
                .phone(phoneNumber)
                .build();
    }

    @Override
    public WxUserVO getUserInfo(String openId) {
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, openId));
        if (ObjectUtils.isEmpty(wxUser)) {
            log.info("未查询到用户信息，openId={}", openId);
            return null;
        }
        log.info("执行成功[查询用户信息]");
        return WxUserVO.fromWxUser(wxUser);
    }

    @Override
    public WxUserVO getUserInfoById(String id) {
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getId, id));
        if (ObjectUtils.isEmpty(wxUser)) {
            log.info("未查询到用户信息，openId={}", id);
            return null;
        }
        log.info("执行成功[查询用户信息]");
        return WxUserVO.fromWxUser(wxUser);
    }

    /**
     * 获取用户手机号码
     * @param sessionKey
     * @param weixinGetPhone
     * @return
     */
    public String getPhone(String sessionKey, WxGetPhoneDTO weixinGetPhone) {
        String result = "";
        byte[] dataByte = new byte[0];
        byte[] keyByte = new byte[0];
        byte[] ivByte = new byte[0];
        try {
            String replace = URLEncoder.encode(weixinGetPhone.getEncryptedData(), "UTF-8").replace("%3D", "=").replace("%2F", "/").replace("%2B", "+");
            dataByte = Base64.decodeBase64(replace);
            String replace2 = URLEncoder.encode(sessionKey, "UTF-8").replace("%3D", "=").replace("%2F", "/").replace("%2B", "+");
            keyByte = org.apache.commons.codec.binary.Base64.decodeBase64(replace2);
            String replace1 = URLEncoder.encode(weixinGetPhone.getIv(), "UTF-8").replace("%3D", "=").replace("%2F", "/").replace("%2B", "+");
            ivByte = org.apache.commons.codec.binary.Base64.decodeBase64(replace1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            // 如果密钥不足16位，那么就补足. 这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String s = resultByte.toString();
                result = new String(resultByte, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //result就是解密后的数据，我这里只需要电话号码，所以进行了数据处理
        System.out.println("result" + result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        return jsonObject.getString("phoneNumber");
    }

    @Override
    public WxUserVO phoneLogin(String phone, String password) {
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getPhone, phone));
        if (ObjectUtils.isEmpty(wxUser)) {
            log.error("手机号未注册");
            return null;
        }
        log.info("执行成功[手机号码登录],phone={}", phone);
        return WxUserVO.fromWxUser(wxUser);
    }

    @Override
    public Boolean updateUser(WxUserDTO req) {
        WxUser wxUser = BeanUtil.copyProperties(req, WxUser.class);
        wxUser.setNickName(req.getNickname());
        return wxUserMapper.update(wxUser, Wrappers.<WxUser>lambdaQuery().eq(WxUser::getOpenId, req.getOpenId())) > 0;
    }

}
