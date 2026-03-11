package com.binbang.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Bean
    public S3Client s3Client(){

        // 1. Credentials 생성
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // 2. CredentialsProvider 생성
        StaticCredentialsProvider credentialsProvider =
                StaticCredentialsProvider.create(credentials);

        // 3.  S3Client 빌드
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .build();
    }
}
