package unitTests;

import com.keepreal.madagascar.common.snowflake.config.IdGeneratorConfiguration;
import com.keepreal.madagascar.common.snowflake.generator.DefaultSnowflakeIdGenerator;
import com.keepreal.madagascar.common.snowflake.model.DefaultSnowflakeId;
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
 * Represents unit tests for {@link DefaultSnowflakeId}.
 */
@SpringBootTest
public class DefaultSnowflakeIdGeneratorUnitTests {

    @Spy
    private IdGeneratorConfiguration configuration;

    @InjectMocks
    @Spy
    private DefaultSnowflakeIdGenerator generator;

    /**
     * Initializes the mock.
     */
    @Before
    public void InitMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests build id.
     */
    @Test
    public void ConstructIdSucceed() {
        // Mock
        Mockito.doReturn(1000L).when(this.generator).currentTimestamp();

        // Action
        long id = this.generator.nextId();

        // Assert
        Assert.assertEquals(id, 4194304000L);
    }

}
