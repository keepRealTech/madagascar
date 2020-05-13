package unitTests;

import com.aliyun.openservices.ons.api.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.common.stats_events.config.StatsEventProducerConfiguration;
import com.keepreal.madagascar.common.stats_events.messageFactory.MessageFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Represents unit tests for {@link MessageFactory}.
 */
@SpringBootTest
public class MessageFactoryUnitTests {

    @Mock
    private StatsEventProducerConfiguration configuration;

    @InjectMocks
    private MessageFactory messageFactory;

    /**
     * Initializes the mock.
     */
    @Before
    public void InitMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests the message factory logic.
     *
     * @throws InvalidProtocolBufferException Exception risen from protocol incompatibility.
     */
    @Test
    public void TestEventMessageBuild() throws InvalidProtocolBufferException {
        StatsEventMessage.Builder messageBuilder = StatsEventMessage.newBuilder();

        // Mock
        Mockito.when(this.configuration.getTopic()).thenReturn("topic");
        Mockito.when(this.configuration.getTag()).thenReturn("tag");

        // Action
        Message message = this.messageFactory.valueOf(messageBuilder);

        // Assert
        StatsEventMessage statsEventMessage = StatsEventMessage.parseFrom(message.getBody());
        Assert.assertEquals(statsEventMessage.getEventId(), message.getKey());
        Assert.assertEquals(statsEventMessage, messageBuilder.build());
    }

}
