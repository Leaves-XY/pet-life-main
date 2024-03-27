package com.yxy.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yxy.pet.client.PredictClient;
import com.yxy.pet.common.basic.response.AppResp;
import com.yxy.pet.common.basic.utils.Base64ToPngConverter;
import com.yxy.pet.common.oss.aliyun.service.AliYunOssService;
import com.yxy.pet.domain.dto.WxUserDTO;
import com.yxy.pet.domain.entity.PredictionResult;
import com.yxy.pet.domain.entity.ResnetResult;
import com.yxy.pet.domain.entity.TranslatedImg;
import com.yxy.pet.mapper.ResnetResultMapper;
import com.yxy.pet.mapper.TranslatedImgMapper;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YeXingyi
 * @version 1.0
 * @date 2024/1/22 21:32
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TranslateService {
    @Autowired
    PredictClient predictClient;

    @Autowired
    private AliYunOssService ossService;

    @Autowired
    private TranslatedImgMapper translatedImgMapper;

    @Autowired
    private ResnetResultMapper resnetResultMapper;

    public AppResp<PredictionResult> predict(String url,String openId) throws IOException {

        TranslatedImg translatedImg = new TranslatedImg();

        translatedImg.setUserOpenId(openId);

        translatedImg.setUrl(url);

        translatedImgMapper.insert(translatedImg);

        Integer translatedId = translatedImg.getId();

        PredictionResult predictionResult =predictClient.predict(url);

        if (predictionResult==null){
            return AppResp.failed(-1L,"解析失败,请联系管理员");
        }
        // 遍历所有预测结果
        for (Map.Entry<String, ResnetResult> entry : predictionResult.getResnetResult().entrySet()) {
            ResnetResult resnetResult = entry.getValue();
            String base64Image = resnetResult.getImg();

            MultipartFile pngImg = Base64ToPngConverter.convertBase64ToMockMultipartFile(base64Image);


            String predictImgName= ossService.uploadObjectOSS(pngImg);

            resnetResult.setImg(predictImgName);

            resnetResult.setPredictId(translatedId.toString());

            resnetResult.setUserOpenId(openId);

            resnetResultMapper.insert(resnetResult);

            log.info("上传图片成功:"+predictImgName,resnetResult);

        }
        log.info("解析成功:"+predictionResult);
        return AppResp.succeed(predictionResult, "解析成功");
    }

    public AppResp<PredictionResult> predictByFile(MultipartFile file,WxUserDTO wxUserDTO) throws IOException {
        String originFileName= ossService.uploadObjectOSS(file);

        TranslatedImg translatedImg = new TranslatedImg();

        translatedImg.setUrl(originFileName);

        translatedImg.setUserOpenId(wxUserDTO.getOpenId());

        translatedImgMapper.insert(translatedImg);

        Integer translatedId = translatedImg.getId();

        File cfile = new File(Objects.requireNonNull(file.getOriginalFilename()));

        file.transferTo(cfile);

        PredictionResult predictionResult =predictClient.predictByFile(file);

        if (predictionResult==null){
            return AppResp.failed(-1L,"解析失败,请联系管理员");
        }
        // 遍历所有预测结果
        for (Map.Entry<String, ResnetResult> entry : predictionResult.getResnetResult().entrySet()) {
            ResnetResult resnetResult = entry.getValue();
            String base64Image = resnetResult.getImg();

            MultipartFile pngImg = Base64ToPngConverter.convertBase64ToMockMultipartFile(base64Image);


            String predictImgName= ossService.uploadObjectOSS(pngImg);

            resnetResult.setImg(predictImgName);

            resnetResult.setPredictId(translatedId.toString());

            resnetResult.setUserOpenId(wxUserDTO.getOpenId());

            resnetResultMapper.insert(resnetResult);

            log.info("上传图片成功:"+predictImgName,resnetResult);

        }
        return AppResp.succeed(predictionResult, "解析成功");
    }

    public AppResp<List<TranslatedImg> > getList(String openId) {
        // 查询用户该openId用户的翻译记录
        QueryWrapper<TranslatedImg> wrapper = new QueryWrapper<>();
        wrapper.eq("user_open_id",openId);
        List<TranslatedImg> translatedImgs = translatedImgMapper.selectList(wrapper);
        return AppResp.succeed(translatedImgs,"查询成功");
    }

    public AppResp deleteById(String id) {
        translatedImgMapper.deleteById(id);
        return AppResp.succeed("删除成功");
    }
}
