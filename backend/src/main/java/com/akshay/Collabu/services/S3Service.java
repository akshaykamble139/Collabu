package com.akshay.Collabu.services;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
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
    
//    public String getUrlForFileInBucket(String bucketName, String key) {
//    	GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileKey)
//                .withMethod(HttpMethod.GET)
//                .withExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)); // URL expires in 1 hour
//
//        // Generate the pre-signed URL
//        URL preSignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//
//        // Print the pre-signed URL
//        System.out.println("Pre-signed URL: " + preSignedUrl.toString());
//        
//        return preSignedUrl.toString();
//    }
}
