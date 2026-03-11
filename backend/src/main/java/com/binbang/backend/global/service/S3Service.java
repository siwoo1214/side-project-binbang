package com.binbang.backend.global.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucket;

    public S3Service(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucket){
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String upLoadFile(MultipartFile file) throws IOException {
        // 1. 원본 파일명 가져오기
        String originalFilename = file.getOriginalFilename();


        // 2. 확장자 추출(jpg 등)
        // originalFilename.lastIndexOf(".") => . 위치 찾기
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        //3. UUID 생성
        //String이지만 UUID 타입 변수에 담고 있는다.
        String uuid = UUID.randomUUID().toString();

        // 4. UUID + 확장자로 새 파일명 생성
        String fileName = uuid + extension;

        // 5. S3에 업로드 (다음 단계)
        // PutObjectRequest => s3에 파일 업로드할 때 필요한 정보를 담는 객체
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        // 6. URL 반환 (다음 단계)
        RequestBody requestBody = RequestBody.fromInputStream(
                file.getInputStream(),
                file.getSize()
        );
        s3Client.putObject(request, requestBody);

        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }
}
