package unitTests;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.brookesia.factory.StatsEventFactory;
import com.keepreal.madagascar.brookesia.model.StatsEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * Represents unit tests for {@link StatsEventFactory}.
 */
public class StatsEventFactoryUnitTests {

    @Spy
    private StatsEventFactory statsEventFactory;

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
    public void TestStatsEventBuildNull() {
        // Action
        StatsEvent event = this.statsEventFactory.valueOf(null);

        // Assert
        Assert.assertNull(event);
    }

    /**
     * Tests the message factory logic.
     */
    @Test
    public void TestStatsEventBuild() {
        // Mock
        StatsEventMessage eventMessage = StatsEventMessage.newBuilder()
                .setEventId("1")
                .setTimestamp(123L)
                .setCategory(StatsEventCategory.STATS_CAT_COMMENT)
                .setAction(StatsEventAction.STATS_ACT_RETRIEVE)
                .setValue("test")
                .setLabel("test")
                .setSucceed(true)
                .setMetadata("test")
                .build();

        // Action
        StatsEvent event = this.statsEventFactory.valueOf(eventMessage);

        // Assert
        Assert.assertEquals(event.getId(), eventMessage.getEventId());
        Assert.assertEquals(event.getTimestamp().longValue(), eventMessage.getTimestamp());
        Assert.assertEquals(event.getCategory(), eventMessage.getCategory());
        Assert.assertEquals(event.getAction(), eventMessage.getAction());
        Assert.assertEquals(event.getValue(), eventMessage.getValue());
        Assert.assertEquals(event.getLabel(), eventMessage.getLabel());
        Assert.assertEquals(event.getSucceed(), eventMessage.getSucceed());
        Assert.assertEquals(event.getMetadata(), eventMessage.getMetadata());
    }

}