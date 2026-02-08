package ru.rtkmagistral.magistralapi.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class MinioConfig {
    @Bean
    public OkHttpClient minioOkHttpClient(
            @Value("${minio.connect-timeout}") Long minioConnectTimeout,
            @Value("${minio.read-timeout}") Long minioReadTimeout,
            @Value("${minio.write-timeout}") Long minioWriteTimeout
    ) {
        Duration connect = Duration.ofSeconds(minioConnectTimeout);
        Duration read = Duration.ofSeconds(minioReadTimeout);
        Duration write = Duration.ofSeconds(minioWriteTimeout);

        return new OkHttpClient.Builder()
                .connectTimeout(connect)
                .readTimeout(read)
                .writeTimeout(write)
                .callTimeout(read.plusSeconds(5))
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    public MinioClient minioClient(
            @Value("${minio.endpoint}") String minioEndpoint,
            @Value("${minio.access-key}") String minioAccessKey,
            @Value("${minio.secret-key}") String minioSecretKey,
            OkHttpClient minioOkHttpClient
    ) {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .httpClient(minioOkHttpClient)
                .build();
    }

    @Bean
    public ApplicationRunner minioBucketInitializer(
            MinioClient minio,
            @Value("${minio.main-bucket-name}") String bucket,
            @Value("${minio.auto-create-bucket}") Boolean autoCreateBucket
    ) {
        return args -> {
            if (!Boolean.TRUE.equals(autoCreateBucket)) {
                return;
            }

            try {
                boolean exists = minio.bucketExists(
                        BucketExistsArgs.builder().bucket(bucket).build()
                );
                if (!exists) {
                    minio.makeBucket(
                            MakeBucketArgs.builder().bucket(bucket).build()
                    );
                }
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new IllegalStateException("MinIO bucket init failed: " + bucket, e);
            }
        };
    }
}
