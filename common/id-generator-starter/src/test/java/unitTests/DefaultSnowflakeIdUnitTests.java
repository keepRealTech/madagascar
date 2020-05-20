package unitTests;

import com.keepreal.madagascar.common.snowflake.config.IdGeneratorConfiguration;
import com.keepreal.madagascar.common.snowflake.model.DefaultSnowflakeId;
import org.junit.Assert;
import org.junit.Test;

public class DefaultSnowflakeIdUnitTests {

    /**
     * Tests build id.
     */
    @Test
    public void ConstructIdSucceed() {
        // Mock
        long timestamp = 1000000000000L;

        // Action
        DefaultSnowflakeId id = new DefaultSnowflakeId(1, timestamp, 1);

        // Assert
        Assert.assertEquals(id.toLong(), 4194304000000004097L);
    }

    /**
     * Tests build id.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ConstructIdSequenceIdOutOfRange() {
        // Mock
        long timestamp = 1000000000000L;
        int sequenceId = IdGeneratorConfiguration.MAX_SEQUENCE + 1;

        // Action
        new DefaultSnowflakeId(1, timestamp, sequenceId);
    }

    /**
     * Tests build id.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ConstructIdNodeIdOutOfRange() {
        // Mock
        long timestamp = 1000000000000L;
        int nodeId = IdGeneratorConfiguration.MAX_NODE_ID + 1;

        // Action
        new DefaultSnowflakeId(nodeId, timestamp, 1);
    }

}
