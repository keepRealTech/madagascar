package com.keepreal.madagascar.coua.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.config.AliyunSmsConfig;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Represents the aliyun sms service service.
 */
@Slf4j
@Service
public class AliyunSmsService {
    private static final String REGION_ID = "cn-beijing";
    private static final String SYS_DOMAIN = "dysmsapi.aliyuncs.com";
    private static final String SYS_VERSION = "2017-05-25";
    private static final String MOBILE_PHONE_LIMIT = "otp_limit_";
    private static final String MOBILE_PHONE_OTP_COUNT = "otp_count_";
    public static final String MOBILE_PHONE_OTP = "otp_";
    private final AliyunSmsConfig aliyunSmsConfig;
    private final Random random;
    private final RedissonClient redissonClient;
    private final Gson gson;

    public AliyunSmsService(AliyunSmsConfig aliyunSmsConfig,
                            RedissonClient redissonClient) {
        this.aliyunSmsConfig = aliyunSmsConfig;
        this.redissonClient = redissonClient;
        this.random = new Random();
        this.gson = new Gson();
    }

    /**
     * use aliyun sms send otp
     *
     * @param mobile mobile phone number
     */
    public CommonStatus sendOtpToMobile(String mobile) {
        Boolean isLimited = this.isMobileOtpLimited(mobile);
        if (isLimited) {
            return CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_OTP_TOO_FREQUENTLY);
        }

        DefaultProfile profile = DefaultProfile.getProfile(REGION_ID, aliyunSmsConfig.getAccessKey(), aliyunSmsConfig.getAccessSecret());
        IAcsClient client = new DefaultAcsClient(profile);

        int otpCode = this.generateOtp();

        CommonRequest request = new CommonRequest();
        CommonResponse response;
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(SYS_DOMAIN);
        request.setSysVersion(SYS_VERSION);
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", REGION_ID);
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", "跳岛");
        request.putQueryParameter("TemplateCode", this.aliyunSmsConfig.getTemplateId());
        request.putQueryParameter("TemplateParam", "{\"code\" : " + otpCode + "}");

        try {
            response = client.getCommonResponse(request);
        } catch (Exception exception) {
            log.info("aliyun sms error : {}", exception.getMessage());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        String resDataCode = this.getCodeFromAliyunSmsResponse(response);

        if (response.getHttpStatus() == 200 && "isv.BUSINESS_LIMIT_CONTROL".equals(resDataCode)) {
            return CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_OTP_TOO_FREQUENTLY);
        }

        this.updateMobileLimitStatus(mobile);
        RBucket<Integer> mobileOtp = this.redissonClient.getBucket(MOBILE_PHONE_OTP + mobile);
        mobileOtp.set(otpCode, 3L, TimeUnit.MINUTES);
        return CommonStatusUtils.getSuccStatus();
    }

    /**
     * 生成四位验证码
     *
     * @return 验证码
     */
    private Integer generateOtp() {
        return random.nextInt(8999) + 1000;
    }

    /**
     * 判断手机号是否被限制 最多 一分钟一条 一天十条
     *
     * @param mobile 手机号
     * @return 没有限制返回false
     */
    private Boolean isMobileOtpLimited(String mobile) {
        RBucket<String> busy = this.redissonClient.getBucket(MOBILE_PHONE_LIMIT + mobile);
        if (Objects.nonNull(busy.get())) {
            return true;
        }
        RBucket<Integer> otpCount = this.redissonClient.getBucket(MOBILE_PHONE_OTP_COUNT + mobile);
        return Objects.nonNull(otpCount.get()) && otpCount.get() > 10;
    }

    /**
     * 根据手机号更新限制状态
     *
     * @param mobile 手机号
     */
    private void updateMobileLimitStatus(String mobile) {
        RBucket<String> limit = this.redissonClient.getBucket(MOBILE_PHONE_LIMIT + mobile);
        limit.trySet("yes", 1L, TimeUnit.MINUTES);

        RBucket<Integer> count = this.redissonClient.getBucket(MOBILE_PHONE_OTP_COUNT + mobile);
        Long todayLastTimestamp = this.getTodayLastTimestamp();
        Integer intCount = count.get();
        if (Objects.isNull(intCount)) {
            count.trySet(1);
            count.expireAt(todayLastTimestamp);
        } else {
            count.set(++ intCount);
            count.expireAt(todayLastTimestamp);
        }
    }

    /**
     * 从阿里云 SMS response 获取 需要的code
     *
     * @param commonResponse {@link CommonResponse}
     * @return code
     */
    @SuppressWarnings("unchecked")
    private String getCodeFromAliyunSmsResponse(CommonResponse commonResponse) {
        HashMap<String, String> data = this.gson.fromJson(commonResponse.getData(), HashMap.class);
        return data.getOrDefault("Code", "");
    }

    /**
     * 获取当天23:59:59时间戳
     *
     * @return 时间戳
     */
    public Long getTodayLastTimestamp() {
        Calendar todayEndTimestamp = Calendar.getInstance();
        todayEndTimestamp.setTime(new Date());
        todayEndTimestamp.set(Calendar.HOUR_OF_DAY, 23);
        todayEndTimestamp.set(Calendar.MINUTE, 59);
        todayEndTimestamp.set(Calendar.SECOND, 59);
        todayEndTimestamp.set(Calendar.MILLISECOND, 0);
        return todayEndTimestamp.getTimeInMillis();
    }

}
