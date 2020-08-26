package com.keepreal.madagascar.workflow.settler.service;

import com.keepreal.madagascar.common.workflow.model.WorkflowLog;
import com.keepreal.madagascar.common.workflow.service.WorkflowService;
import com.keepreal.madagascar.workflow.settler.config.ExecutorConfiguration;
import com.keepreal.madagascar.workflow.settler.config.LarkConfiguration;
import com.keepreal.madagascar.workflow.settler.model.Balance;
import com.keepreal.madagascar.workflow.settler.model.Payment;
import com.keepreal.madagascar.workflow.settler.util.AutoRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Represents the settler service.
 */
@Service
@Slf4j
public class SettlerService {

    private final WorkflowService workflowService;
    private final PaymentService paymentService;
    private final BalanceService balanceService;
    private final RedissonClient redissonClient;
    private final ExecutorService executorService;
    private final ExecutorConfiguration executorConfiguration;
    private final LarkConfiguration larkConfiguration;

    private final RestTemplate restTemplate;

    /**
     * Constructs the settler service.
     *
     * @param workflowService       {@link WorkflowService}.
     * @param paymentService        {@link PaymentService}.
     * @param balanceService        {@link BalanceService}.
     * @param redissonClient        {@link RedissonClient}.
     * @param executorConfiguration {@link ExecutorConfiguration}.
     * @param larkConfiguration     {@link LarkConfiguration}.
     * @param restTemplate          {@link RestTemplate}.
     */
    public SettlerService(WorkflowService workflowService,
                          PaymentService paymentService,
                          BalanceService balanceService,
                          RedissonClient redissonClient,
                          ExecutorConfiguration executorConfiguration,
                          LarkConfiguration larkConfiguration,
                          RestTemplate restTemplate) {
        this.workflowService = workflowService;
        this.paymentService = paymentService;
        this.balanceService = balanceService;
        this.redissonClient = redissonClient;
        this.executorConfiguration = executorConfiguration;
        this.larkConfiguration = larkConfiguration;
        this.restTemplate = restTemplate;
        this.executorService = new ThreadPoolExecutor(
                this.executorConfiguration.getThreads(),
                this.executorConfiguration.getThreads(),
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new CustomizableThreadFactory("settler-"));
    }

    /**
     * The entry point for the workflow.
     */
    public void settlePayments() {
        log.info("Starting workflow [settler].");
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, "settler", 5000, TimeUnit.DAYS.toMillis(1L))) {
            WorkflowLog workflowLog = this.workflowService.initialize(" settling");

            try {
                while (true) {
                    List<Payment> unsettledPayments = this.paymentService.retrieveTop5000UnsettledPayments().stream()
                            .filter(payment -> !StringUtils.isEmpty(payment.getPayeeId()))
                            .collect(Collectors.toList());

                    if (unsettledPayments.isEmpty()) {
                        break;
                    }

                    Map<String, List<Payment>> paymentMap = unsettledPayments.stream()
                            .collect(Collectors.groupingBy(Payment::getPayeeId, HashMap::new, Collectors.toList()));

                    Map<Integer, Map<String, List<Payment>>> batchedPaymentMap = paymentMap.entrySet().stream()
                            .collect(Collectors.groupingBy(
                                    entry -> entry.getKey().hashCode() % this.executorConfiguration.getThreads(),
                                    HashMap::new,
                                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    batchedPaymentMap.forEach((key, value) ->
                            futures.add(CompletableFuture.supplyAsync(() -> this.batchSettle(value), this.executorService)
                                    .thenAccept(succeedIds -> workflowLog.getPaymentIds().addAll(succeedIds))));

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
                }

                long withdrawCount = this.paymentService.countWithdrawPayments();

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                String requestBody = String.format("{\"title\":\"未处理提现请求提醒\", \"text\":\"截止今天共有%d提现尚未处理。\"}", withdrawCount);
                HttpEntity<String> requst = new HttpEntity<>(requestBody, headers);
                this.restTemplate.exchange(this.larkConfiguration.getWebhook(), HttpMethod.POST, requst, String.class);

                this.workflowService.succeed(workflowLog);
            } catch (Exception exception) {
                this.workflowService.failed(workflowLog, exception);
            }
        }
    }

    /**
     * Expires pending payments.
     */
    public void expirePayments() {
        log.info("Starting workflow [settler].");
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, "settler", 5000, TimeUnit.DAYS.toMillis(1L))) {
            WorkflowLog workflowLog = this.workflowService.initialize(" expiring");

            try {
                while (true) {
                    List<Payment> expiredPayments = this.paymentService.retrieveTop5000ExpiredPendingPayments().stream()
                            .filter(payment -> !StringUtils.isEmpty(payment.getPayeeId()))
                            .collect(Collectors.toList());

                    if (expiredPayments.isEmpty()) {
                        break;
                    }

                    Map<String, List<Payment>> paymentMap = expiredPayments.stream()
                            .collect(Collectors.groupingBy(Payment::getPayeeId, HashMap::new, Collectors.toList()));

                    Map<Integer, Map<String, List<Payment>>> batchedPaymentMap = paymentMap.entrySet().stream()
                            .collect(Collectors.groupingBy(
                                    entry -> entry.getKey().hashCode() % this.executorConfiguration.getThreads(),
                                    HashMap::new,
                                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    batchedPaymentMap.forEach((key, value) ->
                            futures.add(CompletableFuture.supplyAsync(() -> this.batchExpire(value), this.executorService)
                                    .thenAccept(succeedIds -> workflowLog.getPaymentIds().addAll(succeedIds))));

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
                }
                this.workflowService.succeed(workflowLog);
            } catch (Exception exception) {
                this.workflowService.failed(workflowLog, exception);
            }
        }
    }

    /**
     * Settles payments by a batch.
     *
     * @param batch A bach of user id and related unsettled payments.
     * @return Successfully settled payment ids.
     */
    public List<String> batchSettle(Map<String, List<Payment>> batch) {
        List<String> succeedIds = new ArrayList<>();

        batch.forEach((key, value) -> {
            Balance userBalance = this.balanceService.retrieveByUserId(key);

            if (Objects.isNull(userBalance)) {
                return;
            }

            succeedIds.addAll(this.settle(userBalance, value));
        });

        return succeedIds;
    }

    /**
     * Expires payments by a batch.
     *
     * @param batch A bach of user id and related expired payments.
     * @return Successfully settled payment ids.
     */
    public List<String> batchExpire(Map<String, List<Payment>> batch) {
        List<String> succeedIds = new ArrayList<>();

        batch.forEach((key, value) -> {
            Balance userBalance = this.balanceService.retrieveByUserId(key);

            if (Objects.isNull(userBalance)) {
                return;
            }

            succeedIds.addAll(this.expires(userBalance, value));
        });

        return succeedIds;
    }

    /**
     * Settles a list of payment for a user.
     *
     * @param userBalance {@link Balance}.
     * @param payments    {@link Payment}.
     * @return Payment ids.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public List<String> settle(Balance userBalance, List<Payment> payments) {
        long amount = this.paymentService.settlePayment(payments);
        this.balanceService.addOnCents(userBalance, amount);

        return payments.stream().map(Payment::getId).collect(Collectors.toList());
    }

    /**
     * Expires a list of payments for a user.
     *
     * @param userBalance {@link Balance}.
     * @param payments    {@link Payment}.
     * @return Payment ids.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public List<String> expires(Balance userBalance, List<Payment> payments) {
        long amount = this.paymentService.expiresPayment(payments);
        this.balanceService.subtractCents(userBalance, amount);

        return payments.stream().map(Payment::getId).collect(Collectors.toList());
    }

}
