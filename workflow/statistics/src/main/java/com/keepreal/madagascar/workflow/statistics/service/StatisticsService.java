package com.keepreal.madagascar.workflow.statistics.service;

import com.keepreal.madagascar.common.workflow.model.WorkflowLog;
import com.keepreal.madagascar.common.workflow.service.WorkflowService;
import com.keepreal.madagascar.workflow.statistics.model.IslandIncrement;
import com.keepreal.madagascar.workflow.statistics.model.IslandInfo;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the main logic entrance.
 */
@Service
public class StatisticsService {

    private final static int ITEMS_PER_MESSAGE = 20;

    private final SubscriptionService subscriptionService;
    private final IslandService islandService;
    private final LarkService larkService;

    private final WorkflowService workflowService;

    /**
     * Constructs the statistics workflow service.
     *
     * @param subscriptionService   {@link SubscriptionService}.
     * @param islandService         {@link IslandService}.
     * @param larkService           {@link LarkService}.
     * @param workflowService       {@link WorkflowService}.
     */
    public StatisticsService(SubscriptionService subscriptionService,
                             IslandService islandService,
                             LarkService larkService,
                             WorkflowService workflowService) {
        this.subscriptionService = subscriptionService;
        this.islandService = islandService;
        this.larkService = larkService;
        this.workflowService = workflowService;
    }

    /**
     * Starts the logic.
     */
    public void run() {
        WorkflowLog workflowLog = this.workflowService.initialize("daily increment report");

        List<IslandIncrement> islandIncrements = this.subscriptionService.retrieveIslandIds();
        if (islandIncrements.isEmpty()) {
            this.larkService.sendMessage("没有哦");
            this.workflowService.succeed(workflowLog);
            return;
        }

        Map<String, BigInteger> islandIncrementMap = islandIncrements.stream()
                .collect(Collectors.toMap(IslandIncrement::getIslandId, IslandIncrement::getIncrement, (mem1, mem2) -> mem1, HashMap::new));

        List<IslandInfo> islands = this.islandService.retrieveIslandsByIds(
                islandIncrements.stream()
                        .map(IslandIncrement::getIslandId)
                        .collect(Collectors.toList()));

        List<List<IslandInfo>> batchedIslands = IntStream.range(0, islands.size() / StatisticsService.ITEMS_PER_MESSAGE + 1)
                .boxed()
                .map(i -> islands.subList(i * StatisticsService.ITEMS_PER_MESSAGE, Math.max(islands.size(), i * StatisticsService.ITEMS_PER_MESSAGE)))
                .collect(Collectors.toList());

        batchedIslands.forEach(islandList -> {
            StringBuilder sb = new StringBuilder();
            islandList
                    .forEach(island -> {
                        String text = String.format("岛id:%s, 岛名:%s, 岛创建时间:%s, 岛民数:%d, 昨日新增岛民数:%d\\n",
                                island.getId(),
                                island.getIslandName(),
                                Instant.ofEpochMilli(island.getCreatedTime()).toString(),
                                island.getIslanderNumber(),
                                islandIncrementMap.getOrDefault(island.getId(), new BigInteger("0")).longValue());
                        sb.append(text);
                        sb.append(System.getProperty("line.separator"));
                    });

            this.larkService.sendMessage(sb.toString());
        });

        this.workflowService.succeed(workflowLog);
    }

}