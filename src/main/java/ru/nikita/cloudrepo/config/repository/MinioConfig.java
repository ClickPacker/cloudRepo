package ru.nikita.cloudrepo.config.repository;


import io.minio.MinioClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${MINIO_HOSTNAME:http://127.0.0.1}")
    private String minioHostname;

    @Value("${MINIO_API_PORT:9000}")
    private int minioPort;

    @Value("${MINIO_SECURE:false}")
    private String minioSecure;

    @Value("${MINIO_ROOT_USER:minioadmin}")
    private String minioUsername;

    @Value("${MINIO_ROOT_PASSWORD:minioadmin}")
    private String minioPassword;

    @Getter
    private final String bucketPattern = "user-%d-files";

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient
                .builder()
                .endpoint(minioHostname, minioPort, Boolean.getBoolean(minioSecure))
                .credentials(minioUsername, minioPassword)
                .build();
    }



}
