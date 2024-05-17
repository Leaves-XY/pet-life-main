package com.yxy.pet.controller;

import com.yxy.pet.common.basic.response.AppResp;
import com.yxy.pet.domain.dto.WxUserDTO;
import com.yxy.pet.domain.entity.PredictionResult;
import com.yxy.pet.domain.entity.TranslatedImg;
import com.yxy.pet.service.impl.TranslateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author YeXingyi
 * @version 1.0
 * @date 2024/3/6 10:38
 */
@Slf4j
@RequestMapping("translate")
@RestController
@AllArgsConstructor
public class TranslateController {
    private TranslateService translateService;
    @PostMapping("predictImg")
    public AppResp<PredictionResult> predict(String url,String openId){
        try {
            return translateService.predict(url,openId);
        } catch (IOException e) {
            return AppResp.failed(-1,"解析失败:"+e.getMessage());
        }
    }

    @GetMapping("getList")
    public AppResp<List<TranslatedImg>> getList(String openId){
//        if(openId==null||openId.isEmpty()){
//            return AppResp.failed(-1,"请登录后再操作");
//        }
        return translateService.getList(openId);
    }

    @GetMapping("getListAndWxyy")
    public AppResp<List<TranslatedImg>> getListAndWxyy(String openId){
//        if(openId==null||openId.isEmpty()){
//            return AppResp.failed(-1,"请登录后再操作");
//        }
        return translateService.getListAndWxyy(openId);
    }

    @PostMapping("deleteById")
    public AppResp deleteById(String id){
        return translateService.deleteById(id);
    }


//    @PostMapping("predictImgByFile")
//    public AppResp<PredictionResult> predictByFile(MultipartFile file/*, @RequestBody WxUserDTO wxUserDTO*/){
//        try {
//            WxUserDTO wxUserDTO1 = new WxUserDTO();
//            wxUserDTO1.setOpenId("oAMiG6weVjAXf15wvfBBTPmT_gsk");
//            return translateService.predictByFile(file, wxUserDTO1);
//        } catch (IOException e) {
//            return AppResp.failed(-1,"解析失败:"+e.getMessage());
//        }
//    }


}
