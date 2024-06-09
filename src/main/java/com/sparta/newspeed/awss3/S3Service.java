package com.sparta.newspeed.awss3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String fileUrl = "https://" + bucket + "/test" + fileName;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> uploadFileList(List<MultipartFile> files) {
        List<String> fileNameList = new ArrayList<>();
        for(MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                String fileUrl = "https://" + bucket + "/test" + fileName;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
                fileNameList.add(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return fileNameList;
    }
    public String readFile(String fileName) {
        URL url = amazonS3Client.getUrl(bucket, fileName);
        String urltext = "" + url;
        return urltext;
    }
    public void deleteFile(String fileName) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }
}
