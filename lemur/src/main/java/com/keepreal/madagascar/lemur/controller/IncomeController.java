package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.IncomeDetailType;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.dtoFactory.IncomeDTOFactory;
import com.keepreal.madagascar.lemur.service.IncomeService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.IncomeMonthlyMessage;
import com.keepreal.madagascar.vanga.IncomeProfileMessage;
import com.keepreal.madagascar.vanga.RetrieveCurrentMonthResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeDetailResponse;
import com.keepreal.madagascar.vanga.RetrieveSupportListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.IncomeApi;
import swagger.model.CurrentMonthSupportResponse;
import swagger.model.GreetingsResponse;
import swagger.model.IncomeDetailsResponse;
import swagger.model.IncomeMonthlyResponse;
import swagger.model.IncomeProfileResponse;
import swagger.model.IncomeSupportsResponse;
import swagger.model.IncomeType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the income controller.
 */
@RestController
@Slf4j
public class IncomeController implements IncomeApi {

    private final IncomeService incomeService;
    private final IncomeDTOFactory incomeDTOFactory;

    public IncomeController(IncomeService incomeService,
                            IncomeDTOFactory incomeDTOFactory) {
        this.incomeService = incomeService;
        this.incomeDTOFactory = incomeDTOFactory;
    }

    @Override
    public ResponseEntity<GreetingsResponse> apiV1IncomeGreetingsGet() {
        GreetingsResponse response = new GreetingsResponse();

        response.setData(Arrays.asList(Templates.INCOME_GREETINGS_CONTENTS.split(",")));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CurrentMonthSupportResponse> apiV1IncomeCurrentMonthGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        RetrieveCurrentMonthResponse currentMonthResponse = this.incomeService.retrieveCurrentMonth(userId);

        CurrentMonthSupportResponse response = new CurrentMonthSupportResponse();
        response.setData(this.incomeDTOFactory.valueOf(currentMonthResponse));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IncomeDetailsResponse> apiV1IncomeDetailByConditionGet(IncomeType type, Long timestamp, String membershipId, Integer page, Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        if (type.equals(IncomeType.MEMBERSHIP) && membershipId == null || type.equals(IncomeType.MONTH) && timestamp == null) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        RetrieveIncomeDetailResponse incomeDetailResponse = this.incomeService.retrieveIncomeDetail(userId, this.convertIncomeType(type), timestamp, membershipId, page, pageSize);

        IncomeDetailsResponse response = new IncomeDetailsResponse();
        response.setData(incomeDetailResponse.getMessageList().stream().map(this.incomeDTOFactory::valueOf).collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(incomeDetailResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IncomeMonthlyResponse> apiV1IncomeMonthlyGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        List<IncomeMonthlyMessage> incomeMonthlyMessages = this.incomeService.retrieveIncomeMonthly(userId);

        IncomeMonthlyResponse response = new IncomeMonthlyResponse();
        response.setData(incomeMonthlyMessages.stream().map(this.incomeDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IncomeProfileResponse> apiV1IncomeProfileGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        IncomeProfileMessage incomeProfileMessage = this.incomeService.retrieveIncomeProfile(userId);

        IncomeProfileResponse response = new IncomeProfileResponse();
        response.setData(this.incomeDTOFactory.valueOf(incomeProfileMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IncomeSupportsResponse> apiV1IncomeSupportListGet(Integer page, Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        RetrieveSupportListResponse supportListResponse = this.incomeService.retrieveSupportList(userId, page, pageSize);

        IncomeSupportsResponse response = new IncomeSupportsResponse();
        response.setData(supportListResponse.getMessageList().stream().map(this.incomeDTOFactory::valueOf).collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(supportListResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private IncomeDetailType convertIncomeType(IncomeType type) {
        switch (type) {
            case MONTH:
                return IncomeDetailType.INCOME_MONTH;
            case SUPPORT:
                return IncomeDetailType.INCOME_SUPPORT;
            case MEMBERSHIP:
                return IncomeDetailType.INCOME_MEMBERSHIP;
            case FEED_CHARGE:
                return IncomeDetailType.INCOME_FEED_CHARGE;
        }
        return null;
    }
}
