package unitTests;

import com.aliyun.openservices.ons.api.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.brookesia.consumer.StatsEventListener;
import com.keepreal.madagascar.brookesia.factory.StatsEventFactory;
import com.keepreal.madagascar.brookesia.model.StatsEvent;
import com.keepreal.madagascar.brookesia.repository.StatsEventRepository;
import com.keepreal.madagascar.brookesia.service.StatsEventService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Represents unit tests for {@link StatsEventListener}.
 */
@SpringBootTest
public class StatsEventListenerUnitTests {

    @Mock
    private StatsEventFactory statsEventFactory;

    @Mock
    private StatsEventRepository statsEventRepository;

    @InjectMocks
    private StatsEventService statsEventService;

    @InjectMocks
    private StatsEventListener statsEventListener;

    /**
     * Initializes the mock.
     */
    @Before
    public void InitMocks() {
        MockitoAnnotations.initMocks(this);
    }


    /**
     * Tests the message factory logic.
     */
    @Test
    public void TestConsume() {
        // Mock
//
//        StatsEvent statsEvent =
//                this.statsEventFactory.toStatsEvent(StatsEventMessage.parseFrom(message.getBody()));
//
//        this.statsEventService.insert(statsEvent);
//
//        // Action
//        Message message = this.messageFactory.valueOf(messageBuilder);
//
//        // Assert
//        StatsEventMessage statsEventMessage = StatsEventMessage.parseFrom(message.getBody());
//        Assert.assertEquals(statsEventMessage.getEventId(), message.getKey());
//        Assert.assertEquals(statsEventMessage, messageBuilder.build());
    }


}
