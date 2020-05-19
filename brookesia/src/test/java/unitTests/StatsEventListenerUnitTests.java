package unitTests;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.brookesia.consumer.StatsEventListener;
import com.keepreal.madagascar.brookesia.factory.StatsEventFactory;
import com.keepreal.madagascar.brookesia.model.StatsEvent;
import com.keepreal.madagascar.brookesia.repository.StatsEventRepository;
import com.keepreal.madagascar.brookesia.service.StatsEventService;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

/**
 * Represents unit tests for {@link StatsEventListener}.
 */
@SpringBootTest
public class StatsEventListenerUnitTests {

    @Mock
    private StatsEventFactory statsEventFactory;

    @Mock
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
    public void TestConsumeSucceed() throws InvalidProtocolBufferException {
        // Mock
        StatsEvent statsEvent = StatsEvent.builder().build();
        StatsEventMessage statsEventMessage = StatsEventMessage.newBuilder().build();
        Message message = new Message("topic", "tag", "key", statsEventMessage.toByteArray());
        Mockito.when(this.statsEventFactory.valueOf(StatsEventMessage.parseFrom(message.getBody())))
                .thenReturn(statsEvent);

        // Action
        Action result = this.statsEventListener.consume(message, null);

        // Assert
        Assert.assertEquals(result, Action.CommitMessage);
    }

    /**
     * Tests the message factory logic.
     */
    @Test
    public void TestConsumeBadFormat() {
        // Mock
        Message message = new Message("topic", "tag", "key", "wrong format".getBytes());

        // Action
        Action result = this.statsEventListener.consume(message, null);

        // Assert
        Assert.assertEquals(result, Action.CommitMessage);
    }

    /**
     * Tests the message factory logic.
     */
    @Test
    public void TestConsumeDupId() throws InvalidProtocolBufferException {
        // Mock
        StatsEventMessage statsEventMessage = StatsEventMessage.newBuilder().build();
        Message message = new Message("topic", "tag", "key", statsEventMessage.toByteArray());
        Mockito.when(this.statsEventFactory.valueOf(StatsEventMessage.parseFrom(message.getBody())))
                .thenThrow(new DuplicateKeyException("dup"));

        // Action
        Action result = this.statsEventListener.consume(message, null);

        // Assert
        Assert.assertEquals(result, Action.CommitMessage);
    }

    /**
     * Tests the message factory logic.
     */
    @Test
    public void TestConsumeOtherExceptions() throws InvalidProtocolBufferException {
        // Mock
        StatsEvent statsEvent = StatsEvent.builder().build();
        StatsEventMessage statsEventMessage = StatsEventMessage.newBuilder().build();
        Message message = new Message("topic", "tag", "key", statsEventMessage.toByteArray());
        Mockito.when(this.statsEventFactory.valueOf(StatsEventMessage.parseFrom(message.getBody())))
                .thenReturn(statsEvent);
        Mockito.doThrow(new MongoClientException("unexpected exception"))
                .when(this.statsEventService).insert(statsEvent);

        // Action
        Action result = this.statsEventListener.consume(message, null);

        // Assert
        Assert.assertEquals(result, Action.ReconsumeLater);
    }

}
