package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.service.OrderService;
import com.keepreal.madagascar.lemur.util.WXPayConstants;
import com.keepreal.madagascar.lemur.util.WXPayUtil;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the wechat pay order callback controller.
 */
@Controller
@Slf4j
public class WechatPayCallbackController {

    private final String successResponseXml;
    private final OrderService orderService;

    /**
     * Constructs the wechat pay callback controller.
     *
     * @param orderService {@link OrderService}.
     */
    @SneakyThrows
    public WechatPayCallbackController(OrderService orderService) {
        this.orderService = orderService;
        Map<String, String> response = new HashMap<>();
        response.put("return_code", WXPayConstants.SUCCESS);
        response.put("return_msg", "OK");
        this.successResponseXml = WXPayUtil.mapToXml(response);
    }

    /**
     * Implements the wechat order callback api.
     *
     * @param request  {@link HttpServletRequest}.
     * @param response {@link HttpServletResponse}.
     */
    @RequestMapping(value = "/api/v1/orders/wechat/callback", method = RequestMethod.POST)
    public void apiV1OrdersWechatCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestXml = IOUtils.toString(request.getInputStream(), Charset.forName(request.getCharacterEncoding()));
            this.orderService.wechatOrderCallback(requestXml);

            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(this.successResponseXml.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Implements the wechat refund order callback api.
     *
     * @param request  {@link HttpServletRequest}.
     * @param response {@link HttpServletResponse}.
     */
    @RequestMapping(value = "/api/v1/orders/wechat/refund/callback", method = RequestMethod.POST)
    public void apiV1OrdersWechatRefundCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestXml = IOUtils.toString(request.getInputStream(), Charset.forName(request.getCharacterEncoding()));
            this.orderService.wechatOrderRefundCallback(requestXml);

            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(this.successResponseXml.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
