package com.keepreal.madagascar.lemur.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.keepreal.madagascar.angonoka.SuperFollowMessage;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.common.enums.MpWechatMsgType;
import com.keepreal.madagascar.lemur.service.FollowService;
import com.keepreal.madagascar.lemur.service.LoginService;
import com.keepreal.madagascar.lemur.service.MpWechatService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.WXPayUtil;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents the mp wechat controller.
 */
@RestController
@Slf4j
public class MpWechatController {
    private final LoginService loginService;
    private final MpWechatService mpWechatService;
    private final FollowService followService;
    private final UserService userService;
    private final RedissonClient redissonClient;

    /**
     * Constructs the mp wechat controller.
     *
     * @param loginService {@link LoginService}
     * @param mpWechatService {@link MpWechatService}
     * @param followService {@link FollowService}
     * @param userService {@link UserService}
     * @param redissonClient {@link RedissonClient}
     */
    public MpWechatController(LoginService loginService,
                              MpWechatService mpWechatService,
                              FollowService followService,
                              UserService userService,
                              RedissonClient redissonClient) {
        this.loginService = loginService;
        this.mpWechatService = mpWechatService;
        this.followService = followService;
        this.userService = userService;
        this.redissonClient = redissonClient;
    }

    /**
     * initialize and verify wechat official accounts server
     *
     * @param signature wechat server signature
     * @param timestamp wechat server timestamp
     * @param nonce     wechat server random number
     * @param echostr   wechat server random string
     * @return check successful return echostr
     */
    @GetMapping("/api/v1/events/wechatMp/callback")
    public String verifyWechatServer(String signature, String timestamp, String nonce, String echostr) {
        if (Objects.nonNull(signature) && Objects.nonNull(timestamp) && Objects.nonNull(nonce) && Objects.nonNull(echostr)) {
            CheckSignatureRequest request = CheckSignatureRequest.newBuilder().setSignature(signature).setTimestamp(timestamp).setNonce(nonce).build();
            if (this.loginService.checkSignature(request)) {
                return echostr;
            }
        }
        return null;
    }

    /**
     * receive wechat server event
     */
    @PostMapping("/api/v1/events/wechatMp/callback")
    public String receiveWechatServerEventPush(HttpServletRequest httpServletRequest) throws Exception {
        String requestXml = IOUtils.toString(httpServletRequest.getInputStream(), Charset.forName(httpServletRequest.getCharacterEncoding()));
        Map<String, String> request = WXPayUtil.xmlToMap(requestXml);

        log.info("request info {}", request.toString());

        String fromUserName = request.get("FromUserName");
        String msgType = request.get("MsgType");
        String event = request.get("Event");
        String eventKey = request.get("EventKey");

        //事件推送
        if (Objects.nonNull(fromUserName) && Objects.nonNull(msgType) && Objects.nonNull(event) && Objects.nonNull(eventKey)) {
            if ("event".equals(msgType)) {
                this.loginService.handleEvent(fromUserName, event, eventKey);
                return "success";
            }
        }

        //普通消息
        if (MpWechatMsgType.TEXT.getValue().equals(msgType)) {
            String msgId = request.get("MsgId");

            log.info("msg id is {}", msgId);

            RBucket<String> bucket = this.redissonClient.getBucket(Constants.MP_WECHAT_DEDUPLICATION_KEY + msgId);
            if (bucket.isExists()) {
                bucket.delete();
                return "success";
            }
            String openId = request.get("FromUserName");
            String keepRealMpId = request.get("ToUserName");
            String content = request.get("Content");

            SuperFollowMessage superFollowMessage = this.followService.retrieveSuperFollowByCode(content);

            log.info("super follow {} ", Objects.isNull(superFollowMessage) ? "null" : superFollowMessage.toString());

            if (Objects.nonNull(superFollowMessage)) {
                this.followService.createSuperFollowSubscription(openId, superFollowMessage.getHostId(), superFollowMessage.getId());

                log.info("reply");

                String replyXml = this.generateSuccessReplyXml(superFollowMessage.getHostId(),
                        superFollowMessage.getIslandId(),
                        openId,
                        keepRealMpId);

                log.info("reply is {}", replyXml);

                RBucket<String> flag = this.redissonClient.getBucket(Constants.MP_WECHAT_DEDUPLICATION_KEY + msgId);
                flag.set("true", 30, TimeUnit.SECONDS);
                return replyXml;
            }
            return "success";
        }
        return "success";
    }

    /**
     * 成功订阅回复文案
     *
     * @param hostId host id
     * @param islandId island id
     * @return 文案
     */
    @SneakyThrows
    private String generateSuccessReplyXml (String hostId, String islandId, String openId, String keepRealMpId) {
        UserMessage userMessage = this.userService.retrieveUserById(hostId);
        String name = userMessage.getName();
        String h5Url = String.format(Templates.H5_REPOST_URL,
                islandId,
                hostId);
        String replyContent = String.format(Templates.FOLLOW_SUCCESS_CONTENTS, name, name, name, h5Url, name);
        XmlMapper xmlMapper = new XmlMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("ToUserName", openId);
        map.put("FromUserName", keepRealMpId);
        map.put("CreateTime", Instant.now().toEpochMilli());
        map.put("MsgType", MpWechatMsgType.TEXT.getValue());
        map.put("Content", replyContent);
        return xmlMapper.writeValueAsString(map);
    }

}
