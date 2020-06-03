package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.ReportType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.ReportMessage;
import com.keepreal.madagascar.lemur.dtoFactory.ReportDTOFactory;
import com.keepreal.madagascar.lemur.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ReportApi;
import swagger.model.PostReportRequest;
import swagger.model.ReportResponse;

import javax.validation.Valid;

/**
 * Represents the report controller.
 */
@RestController
public class ReportController implements ReportApi {

    private final ReportService reportService;
    private final ReportDTOFactory reportDTOFactory;

    /**
     * Constructs the report controller.
     *
     * @param reportService    Report service.
     * @param reportDTOFactory Report dto factory.
     */
    public ReportController(ReportService reportService, ReportDTOFactory reportDTOFactory) {
        this.reportService = reportService;
        this.reportDTOFactory = reportDTOFactory;
    }

    /**
     * Implements the post new report api.
     *
     * @param postReportRequest {@link PostReportRequest}.
     * @return {@link ReportResponse}.
     */
    @Override
    public ResponseEntity<ReportResponse> apiV1ReportsPost(@Valid PostReportRequest postReportRequest) {
        if ((StringUtils.isEmpty(postReportRequest.getFeedId()) && StringUtils.isEmpty(postReportRequest.getIslandId()))
                || (!StringUtils.isEmpty(postReportRequest.getFeedId()) && !StringUtils.isEmpty(postReportRequest.getIslandId()))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReportMessage reportMessage = this.reportService.createReport(postReportRequest.getFeedId(),
                postReportRequest.getIslandId(),
                postReportRequest.getReporterId(),
                this.convertReportType(postReportRequest.getReportType()));

        ReportResponse response = new ReportResponse();
        response.setData(this.reportDTOFactory.valueOf(reportMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts the report type.
     *
     * @param type Type.
     * @return {@link ReportType}.
     */
    private ReportType convertReportType(String type) {
        return ReportType.valueOf(type);
    }

}
