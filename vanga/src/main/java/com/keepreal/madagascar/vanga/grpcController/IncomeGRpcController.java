package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.IncomeDetailType;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.vanga.IncomeServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveCurrentMonthResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeDetailRequest;
import com.keepreal.madagascar.vanga.RetrieveIncomeDetailResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeMonthlyResponse;
import com.keepreal.madagascar.vanga.RetrieveIncomeProfileResponse;
import com.keepreal.madagascar.vanga.RetrieveMyIncomeRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportListRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportListResponse;
import com.keepreal.madagascar.vanga.factory.IncomeMessageFactory;
import com.keepreal.madagascar.vanga.model.IncomeDetail;
import com.keepreal.madagascar.vanga.model.IncomeProfile;
import com.keepreal.madagascar.vanga.model.IncomeSupport;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.service.IncomeService;
import com.keepreal.madagascar.vanga.service.MembershipService;
import com.keepreal.madagascar.vanga.service.PaymentService;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import com.keepreal.madagascar.vanga.util.DateUtils;
import com.keepreal.madagascar.vanga.util.PaginationUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@GRpcService
public class IncomeGRpcController extends IncomeServiceGrpc.IncomeServiceImplBase {

    private final IncomeService incomeService;
    private final PaymentService paymentService;
    private final MembershipService membershipService;
    private final SkuService skuService;
    private final IncomeMessageFactory incomeMessageFactory;

    public IncomeGRpcController(IncomeService incomeService,
                                PaymentService paymentService,
                                MembershipService membershipService,
                                SkuService skuService,
                                IncomeMessageFactory incomeMessageFactory) {
        this.incomeService = incomeService;
        this.paymentService = paymentService;
        this.membershipService = membershipService;
        this.skuService = skuService;
        this.incomeMessageFactory = incomeMessageFactory;
    }

    @Override
    public void retrieveIncomeProfile(RetrieveMyIncomeRequest request, StreamObserver<RetrieveIncomeProfileResponse> responseObserver) {
        String userId = request.getUserId();
        IncomeProfile incomeProfile = this.incomeService.findIncomeProfileByUserId(userId);

        responseObserver.onNext(RetrieveIncomeProfileResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setMessage(this.incomeMessageFactory.profileValueOf(userId, incomeProfile))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveSupportList(RetrieveSupportListRequest request, StreamObserver<RetrieveSupportListResponse> responseObserver) {
        String userId = request.getUserId();
        PageRequest pageRequest = request.getPageRequest();

        Page<IncomeSupport> incomeSupportPage = this.incomeService.findIncomeSupportsByUserId(userId, PaginationUtils.valueOf(pageRequest));

        responseObserver.onNext(RetrieveSupportListResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setPageResponse(PaginationUtils.valueOf(incomeSupportPage, pageRequest))
                .addAllMessage(incomeSupportPage.getContent().stream().map(this.incomeMessageFactory::valueOf).collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveCurrentMonth(RetrieveMyIncomeRequest request, StreamObserver<RetrieveCurrentMonthResponse> responseObserver) {
        String userId = request.getUserId();

        List<MembershipMessage> membershipMessageList = this.membershipService.retrieveMembershipsByUserId(userId);

        responseObserver.onNext(RetrieveCurrentMonthResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMembershipMessage(membershipMessageList.stream().map(this.incomeMessageFactory::valueOf).collect(Collectors.toList()))
                .setSponsorMessage(this.incomeMessageFactory.sponsorValueOf(userId))
                .setFeedChargeMessage(this.incomeMessageFactory.feedChargeValueOf(userId))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIncomeMonthly(RetrieveMyIncomeRequest request, StreamObserver<RetrieveIncomeMonthlyResponse> responseObserver) {
        String userId = request.getUserId();

        List<IncomeDetail> incomeDetailList = this.incomeService.findIncomeDetailsByUserId(userId);

        responseObserver.onNext(RetrieveIncomeMonthlyResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMessage(incomeDetailList.stream().map(this.incomeMessageFactory::valueOf).collect(Collectors.toList()))
                .build());
    }

    @Override
    public void retrieveIncomeDetail(RetrieveIncomeDetailRequest request, StreamObserver<RetrieveIncomeDetailResponse> responseObserver) {
        String userId = request.getUserId();
        IncomeDetailType incomeDetailType = request.getType();
        PageRequest pageRequest = request.getPageRequest();

        Page<Payment> paymentPage;

        if (incomeDetailType.equals(IncomeDetailType.INCOME_MONTH)) {
            long value = request.getTimestamp().getValue();
            paymentPage = this.paymentService.retrievePaymentsByPayeeId(userId, DateUtils.startOfMonthTimestamp(value), DateUtils.endOfMonthTimestamp(value), PaginationUtils.valueOf(pageRequest));
        } else if (incomeDetailType.equals(IncomeDetailType.INCOME_MEMBERSHIP)) {
            String membershipId = request.getMembershipId().getValue();
            List<MembershipSku> membershipSkus = skuService.retrieveMembershipSkusByMembershipId(membershipId);
            List<String> membershipSkuIdList = membershipSkus.stream().map(MembershipSku::getId).collect(Collectors.toList());
            paymentPage = this.paymentService.retrieveMembershipPaymentsByPayeeId(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp(), membershipSkuIdList, PaginationUtils.valueOf(pageRequest));
        } else if (incomeDetailType.equals(IncomeDetailType.INCOME_SUPPORT)) {
            paymentPage = this.paymentService.retrieveSponsorPaymentsByPayeeId(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp(), PaginationUtils.valueOf(pageRequest));
        } else {
            paymentPage = this.paymentService.retrieveFeedChargePaymentsByPayeeId(userId, DateUtils.startOfMonthTimestamp(), DateUtils.endOfMonthTimestamp(), PaginationUtils.valueOf(pageRequest));
        }

        responseObserver.onNext(RetrieveIncomeDetailResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setPageResponse(PaginationUtils.valueOf(paymentPage, pageRequest))
                .addAllMessage(paymentPage.getContent().stream().map(this.incomeMessageFactory::valueOf).collect(Collectors.toList()))
                .build());
    }
}
