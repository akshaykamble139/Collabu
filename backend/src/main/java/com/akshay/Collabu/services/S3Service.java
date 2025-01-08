package com.akshay.Collabu.services;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private final S3Client s3Client;

    public S3Service(@Value("${aws.access.key.id}") String AWS_ACCESS_KEY_ID,
            @Value("${aws.secret.access.key}") String AWS_SECRET_ACCESS_KEY) {
    	
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                AWS_ACCESS_KEY_ID, // Replace with your AWS Access Key
                AWS_SECRET_ACCESS_KEY // Replace with your AWS Secret Key
        );

        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_1) // Replace with your bucket's region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public void uploadFile(String bucketName, String key, Path filePath) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                filePath
        );
    }
    
    public byte[] downloadFile(String bucketName, String s3Key) {
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build()
        );
        return objectBytes.asByteArray();
    }
}
