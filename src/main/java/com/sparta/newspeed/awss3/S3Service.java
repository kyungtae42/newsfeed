package com.sparta.newspeed.awss3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sparta.newspeed.common.exception.CustomException;
import com.sparta.newspeed.common.exception.ErrorCode;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public String uploadFile(MultipartFile file, String category) {
        String fileName = "";
        try {
            if(category.equals("profile")) {
                fileName = "profile/" + createFileName(file);
            } else if (category.equals("newsfeed")) {
                fileName = "newsfeed/" + createFileName(file);
            }
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
                String fileName = "newsfeed/" + createFileName(file);
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
    public String createFileName(MultipartFile file) {
        return UUID.randomUUID().toString().concat(validateFile(file));
    }
    private String validateFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        ArrayList<String> imgValidate = new ArrayList<>();
        ArrayList<String> videoValidate = new ArrayList<>();
        imgValidate.add(".jpg");
        imgValidate.add(".jpeg");
        imgValidate.add(".png");
        imgValidate.add(".JPG");
        imgValidate.add(".JPEG");
        imgValidate.add(".PNG");
        videoValidate.add(".mp4");
        videoValidate.add(".avi");
        videoValidate.add(".gif");
        videoValidate.add(".MP4");
        videoValidate.add(".AVI");
        videoValidate.add(".GIF");
        String idxFileName = fileName.substring(fileName.lastIndexOf("."));
        if (imgValidate.contains(idxFileName) || videoValidate.contains(idxFileName)) {
            if (imgValidate.contains(idxFileName) && file.getSize() <= 10240L) {
                throw new CustomException(ErrorCode.IMG_SIZE_OUTOFRANGE);
            } else if (videoValidate.contains(idxFileName) && file.getSize() <= 204800L) {
                throw new CustomException(ErrorCode.VIDEO_SIZE_OUTOFRANGE);
            }
        } else {
            throw new CustomException(ErrorCode.WRONG_FILE_FORMAT);
        }
        return file.getOriginalFilename();
    }
}
