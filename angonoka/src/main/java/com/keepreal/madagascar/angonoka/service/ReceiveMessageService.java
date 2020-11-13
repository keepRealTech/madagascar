package com.keepreal.madagascar.angonoka.service;


import com.keepreal.madagascar.angonoka.api.WeiboApi;
import com.keepreal.madagascar.angonoka.config.WeiboBusinessConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
public class ReceiveMessageService {
    private static transient long sinceId = -1L;
    private final int recBufSize = 256;
    private String receiveInfoUrl;
    private DataInputStream inputStream;
    private byte[] recBuf;
    private int recIndex;
    private final WeiboBusinessConfig weiboBusinessConfig;
    private final FollowService followService;

    public ReceiveMessageService(WeiboBusinessConfig weiboBusinessConfig,
                                 FollowService followService) {
        this.weiboBusinessConfig = weiboBusinessConfig;
        this.followService = followService;
    }

    /**
     * 启动获取数据线程
     */
    public void init() {
        this.receiveInfoUrl = String.format(WeiboApi.COMMERCIAL_PUSH, this.weiboBusinessConfig.getSubId());
        new ReadTask().start();
    }

    /**
     * 获取数据线程
     */
    class ReadTask extends Thread {

        /**
         * 启一个线程从服务器读取数据
         */
        @Override
        public void run() {
            boolean hasError = false;
            while (!hasError) {
                HttpURLConnection connection = null;
                recIndex = 0;
                recBuf = new byte[recBufSize];
                try {
                    connection = connectServer(sinceId);
                    while (true) {
                        processLine();
                    }
                } catch (Exception e) {
                    // 当连接断开时，重新连接
                    System.out.println("connection close: " + e.getMessage());
                    if (e.getMessage().contains("errorCode")) {
                        hasError = true;
                    }
                    System.out.println("last since_id: " + sinceId);
                } finally {

                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    System.out.println(new Date().toString());
                }
            }
        }

        /**
         * 建立http连接
         *
         * @return
         */
        private HttpURLConnection connectServer(long sinceId) throws Exception {
            String targetURL = receiveInfoUrl;
            // 从指定的since_id开始读取数据，保证读取数据的连续性，消息完整性
            if (sinceId > 0L) {
                targetURL = targetURL + "&since_id=" + sinceId;
            }
            System.out.println("get url: " + targetURL);

            URL url = new URL(targetURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int statusCode;
            try {
                statusCode = connection.getResponseCode();
            } catch (Exception e) {
                connection.disconnect();
                throw new Exception("stream url connect failed", e);
            }

            if (statusCode != HttpStatus.OK.value()) {
                throw new RuntimeException(connection.getResponseMessage());
            }

            try {
                inputStream = new DataInputStream(connection.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException("get stream input io exception", e);
            }

            return connection;
        }

        /**
         * 读取并处理数据
         *
         * @throws IOException
         */
        private void processLine() throws IOException {
            byte[] bytes = readLineBytes();
            if ((bytes != null) && (bytes.length > 0)) {
                String message = new String(bytes);
                handleMessage(message);
            }
        }

        /**
         * 可以重写此方法解析message
         *
         * @param message
         */
        private void handleMessage(String message) {
            followService.handleWeiboSubscriptionMessage(message);
        }

        /**
         * 读取数据
         *
         * @return
         * @throws IOException
         */
        public byte[] readLineBytes() throws IOException {
            byte[] result;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int readCount;
            if ((recIndex > 0) && (read(bos))) {
                return bos.toByteArray();
            }
            while ((readCount = inputStream.read(recBuf, recIndex, recBuf.length - recIndex)) > 0) {
                recIndex = (recIndex + readCount);
                if (read(bos)) {
                    break;
                }
            }
            result = bos.toByteArray();
            if (result == null || result.length <= 0 && recIndex <= 0) {
                throw new IOException("no data in 5 second");
            }
            return result;
        }

        /**
         * 读数据到bos
         *
         * @param bos
         * @return
         */
        private boolean read(ByteArrayOutputStream bos) {
            boolean result = false;
            int index = -1;
            for (int i = 0; i < recIndex - 1; i++) {
                // 13cr-回车 10lf-换行
                if ((recBuf[i] == 13) && (recBuf[(i + 1)] == 10)) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                bos.write(recBuf, 0, index);
                byte[] newBuf = new byte[recBufSize];
                if (recIndex > index + 2) {
                    System.arraycopy(recBuf, index + 2, newBuf, 0, recIndex - index - 2);
                }
                recBuf = newBuf;
                recIndex = (recIndex - index - 2);
                result = true;
            } else if (recBuf[(recIndex - 1)] == 13) {
                bos.write(recBuf, 0, recIndex - 1);
                Arrays.fill(recBuf, (byte) 0);
                recBuf[0] = 13;
                recIndex = 1;
            } else {
                bos.write(recBuf, 0, recIndex);
                Arrays.fill(recBuf, (byte) 0);
                recIndex = 0;
            }

            return result;
        }
    }


}
