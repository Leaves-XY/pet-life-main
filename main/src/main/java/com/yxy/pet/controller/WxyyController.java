package com.yxy.pet.controller;

import com.yxy.pet.common.basic.response.AppResp;
import com.yxy.pet.domain.entity.PredictionResult;
import com.yxy.pet.domain.entity.TranslatedImg;
import com.yxy.pet.domain.entity.WxUser;
import com.yxy.pet.service.WxUserService;
import com.yxy.pet.service.impl.TranslateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author YeXingyi
 * @version 1.0
 * @date 2024/4/12 9:02
 */
@Slf4j
@RequestMapping("wxyy")
@RestController
@AllArgsConstructor
public class WxyyController {
    private TranslateService translateService;
    private WxUserService wxUserService;
    @PostMapping("savePredictResult")
    public AppResp<Integer> savePredictResult(String url,@RequestBody PredictionResult predictionResult){
        return translateService.saveWxyyPredictResult(url,predictionResult);
    }

    @PostMapping("savePredictResultAllUser")
    public AppResp<PredictionResult> savePredictResultAllUser(String url,@RequestBody PredictionResult predictionResult){

        return translateService.saveWxyyPredictResultAllUser(url,predictionResult);
    }



    @PostMapping("savePredictResultMatchPhone")
    public AppResp<TranslatedImg> savePredictResultMatchPhone(String id,String phone){
        WxUser wxUser = wxUserService.getUserInfoByPhone(phone);
        if(wxUser==null){
            return AppResp.failed(-1,"用户不存在");
        }
        return translateService.savePredictResultMatchPhone(id,wxUser.getOpenId());
    }

    @PostMapping("savePredictResultByPhone")
    public AppResp<Integer> savePredictResultByPhone(String phone,String url,@RequestBody PredictionResult predictionResult){
        WxUser wxUser = wxUserService.getUserInfoByPhone(phone);
        if(wxUser==null){
            return AppResp.failed(-1,"用户不存在");
        }
        return translateService.saveWxyyPredictResult(url,predictionResult);
    }

}


