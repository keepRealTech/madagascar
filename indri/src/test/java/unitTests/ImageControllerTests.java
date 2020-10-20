package unitTests;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectResult;
import com.google.protobuf.ByteString;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.indri.MigrateImageRequest;
import com.keepreal.madagascar.indri.UploadImagesRequest;
import com.keepreal.madagascar.indri.config.AliyunOssConfiguration;
import com.keepreal.madagascar.indri.grpcController.ImageGRpcController;
import com.keepreal.madagascar.indri.util.CommonStatusUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents image controller tests.
 */
@SpringBootTest
public class ImageControllerTests {

    private final String bucketName = "bucket";

    @Mock
    private OSS ossClientMock;

    @Mock
    private AliyunOssConfiguration aliyunOssConfiguration;

    @Spy
    private CommonStatusUtils commonStatusUtils;

    @InjectMocks
    private ImageGRpcController imageGRpcController;

    private MockWebServer mockBackEnd;

    /**
     * Initializes the mock.
     */
    @Before
    public void InitMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.imageGRpcController, "bucketName", this.bucketName);

        this.mockBackEnd = new MockWebServer();
        this.mockBackEnd.start();
    }

    /**
     * Tears down.
     *
     * @throws IOException {@link IOException}.
     */
    @After
    public void tearDown() throws IOException {
        this.mockBackEnd.shutdown();
    }

    /**
     * Represents the upload success test.
     */
    @Test
    public void uploadSuccess() {
        List<String> names = Arrays.asList("1.jpg", "2.jpg");
        List<ByteString> contents = names.stream()
                .map(name -> ByteString.copyFrom(name.getBytes()))
                .collect(Collectors.toList());

        UploadImagesRequest request = UploadImagesRequest.newBuilder()
                .addAllImageContent(contents)
                .addAllImageNames(names)
                .build();

        Mockito.when(this.ossClientMock.putObject(Mockito.eq(this.bucketName),
                Mockito.anyString(),
                Mockito.any(InputStream.class)))
                .thenReturn(new PutObjectResult());

        Mono<CommonStatus> mono = this.imageGRpcController.uploadImages(Mono.just(request));

        StepVerifier.create(mono)
                .expectNext(this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .verifyComplete();

        Mockito.verify(this.ossClientMock, Mockito.times(names.size())).putObject(Mockito.eq(this.bucketName),
                Mockito.anyString(),
                Mockito.any(InputStream.class));
    }

    /**
     * Represents the upload failure test.
     */
    @Test
    public void uploadFailed() {
        List<String> names = Collections.singletonList("1.jpg");
        List<ByteString> contents = Collections.singletonList(
                ByteString.copyFrom(names.get(0).getBytes()));

        UploadImagesRequest request = UploadImagesRequest.newBuilder()
                .addAllImageContent(contents)
                .addAllImageNames(names)
                .build();

        Mockito.when(this.ossClientMock.putObject(Mockito.eq(this.bucketName),
                Mockito.anyString(),
                Mockito.any(InputStream.class)))
                .thenThrow(new OSSException());

        Mono<CommonStatus> mono = this.imageGRpcController.uploadImages(Mono.just(request));

        StepVerifier.create(mono)
                .expectNext(this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_IMAGE_UPLOAD_ERROR))
                .verifyComplete();

        Mockito.verify(this.ossClientMock, Mockito.times(names.size())).putObject(Mockito.eq(this.bucketName),
                Mockito.anyString(),
                Mockito.any(InputStream.class));
    }

    /**
     * Represents the upload empty list test.
     */
    @Test
    public void uploadEmpty() {
        UploadImagesRequest request = UploadImagesRequest.newBuilder()
                .build();

        Mockito.when(this.ossClientMock.putObject(Mockito.eq(this.bucketName),
                Mockito.anyString(),
                Mockito.any(InputStream.class)))
                .thenThrow(new OSSException());

        Mono<CommonStatus> mono = this.imageGRpcController.uploadImages(Mono.just(request));

        StepVerifier.create(mono)
                .expectNext(this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .verifyComplete();

        Mockito.verify(this.ossClientMock, Mockito.times(0)).putObject(Mockito.eq(this.bucketName),
                Mockito.anyString(),
                Mockito.any(InputStream.class));
    }

    /**
     * Represents the migrate image success test.
     */
    @Test
    public void migrateSuccess() {
        MigrateImageRequest request = MigrateImageRequest.newBuilder()
                .setSourceUrl(String.format("http://localhost:%s", this.mockBackEnd.getPort()))
                .setDestinationUri("destination")
                .build();

        this.mockBackEnd.enqueue(new MockResponse()
                .setBody("test")
                .addHeader("Content-Type", MediaType.IMAGE_JPEG));

        Mockito.when(this.ossClientMock.putObject(Mockito.eq(this.bucketName),
                Mockito.eq(request.getDestinationUri()),
                Mockito.refEq(new ByteArrayInputStream("test".getBytes()))))
                .thenReturn(new PutObjectResult());

        Mono<CommonStatus> mono = this.imageGRpcController.migrateImage(Mono.just(request));

        StepVerifier.create(mono)
                .expectNext(this.commonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .verifyComplete();

        Mockito.verify(this.ossClientMock, Mockito.times(1)).putObject(Mockito.eq(this.bucketName),
                Mockito.eq(request.getDestinationUri()),
                Mockito.refEq(new ByteArrayInputStream("test".getBytes())));
    }

}
