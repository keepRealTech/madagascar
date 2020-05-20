package unitTests;

import com.keepreal.madagascar.common.snowflake.config.IdGeneratorConfiguration;
import com.keepreal.madagascar.common.snowflake.generator.DefaultSnowflakeIdGenerator;
import com.keepreal.madagascar.common.snowflake.model.DefaultSnowflakeId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    /**
     * Tests build id concurrently.
     *
     * @throws InterruptedException Exception.
     */
    @Test
    public void MultiConstructsSucceed() throws InterruptedException {
        // Mock
        int threads = 4;
        Mockito.doReturn(1000L).when(this.generator).currentTimestamp();

        ExecutorService service = Executors.newFixedThreadPool(threads);

        //Action
        List<Long> syncList = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < threads; i++) {
            service.submit(() -> syncList.add(this.generator.nextId()));
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.SECONDS);

        // Assert
        Assert.assertEquals(syncList.size(), 4);
        Assert.assertTrue(syncList.contains(4194304000L));
        Assert.assertTrue(syncList.contains(4194304001L));
        Assert.assertTrue(syncList.contains(4194304002L));
        Assert.assertTrue(syncList.contains(4194304003L));
    }

}
