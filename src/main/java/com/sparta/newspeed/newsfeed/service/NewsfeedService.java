package com.sparta.newspeed.newsfeed.service;

import com.sparta.newspeed.awss3.S3Service;
import com.sparta.newspeed.common.exception.CustomException;
import com.sparta.newspeed.common.exception.ErrorCode;
import com.sparta.newspeed.newsfeed.dto.NewsfeedRequestDto;
import com.sparta.newspeed.newsfeed.dto.NewsfeedResponseDto;
import com.sparta.newspeed.newsfeed.entity.Newsfeed;
import com.sparta.newspeed.newsfeed.entity.NewsfeedImg;
import com.sparta.newspeed.newsfeed.entity.Ott;
import com.sparta.newspeed.newsfeed.repository.NewsfeedImgRepository;
import com.sparta.newspeed.newsfeed.repository.NewsfeedRespository;
import com.sparta.newspeed.newsfeed.repository.OttRepository;
import com.sparta.newspeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsfeedService {

    private final NewsfeedRespository newsfeedRespository;
    private final NewsfeedImgRepository newsfeedImgRepository;
    private final OttRepository ottRepository;
    private final S3Service s3Service;

    public List<NewsfeedResponseDto> getNewsfeeds() {
        List<Newsfeed> newsfeedList = newsfeedRespository.findAllByOrderByCreatedAtDesc();
        if(newsfeedList.isEmpty()){
            throw new CustomException(ErrorCode.NEWSFEED_EMPTY);
        }
        return newsfeedList.stream().map(NewsfeedResponseDto::new)
                .toList();
    }
    public NewsfeedResponseDto getNewsfeed(Long newsfeedSeq) {
        Newsfeed newsfeed = findNewsfeed(newsfeedSeq);
        List<NewsfeedImg> fileList = newsfeed.getImgList();

        NewsfeedResponseDto responseDto = new NewsfeedResponseDto(newsfeed);
        if(fileList != null) {
            List<String> fileUrlList = new ArrayList<>();
            for(NewsfeedImg file : fileList) {
                fileUrlList.add(s3Service.readFile(file.getFileName()));
            }
            responseDto.setFileUrlList(fileUrlList);
        }
        return responseDto;
    }

    public NewsfeedResponseDto createNewsFeed(NewsfeedRequestDto request, List<MultipartFile> fileList, User user) {
        Ott ott = findOtt(request);
        List<String> fileNameList = null;
        List<String> fileUrlList = new ArrayList<>();
        if (!isRemainMembersValid(ott, request.getRemainMember())) {
            throw new CustomException(ErrorCode.NEWSFEED_REMAIN_MEMBER_OVER);
        }

        // Newsfeed 엔티티 생성 및 저장
        Newsfeed newsfeed = Newsfeed.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .remainMember(request.getRemainMember())
                .user(user)
                .ott(ott)
                .build();
        Newsfeed saveNewsfeed = newsfeedRespository.save(newsfeed);
        NewsfeedResponseDto responseDto = new NewsfeedResponseDto(saveNewsfeed);
        if(fileList != null) {
            fileNameList = s3Service.uploadFileList(fileList);
            for(String fileName : fileNameList) {
                NewsfeedImg img = NewsfeedImg.builder()
                        .fileName(fileName)
                        .newsFeed(newsfeed)
                        .build();
                newsfeedImgRepository.save(img);
                fileUrlList.add(s3Service.readFile(img.getFileName()));
            }
        }
        responseDto.setFileUrlList(fileUrlList);
        return responseDto;
    }

    @Transactional
    public NewsfeedResponseDto updateNewsFeed(Long newsfeedSeq, NewsfeedRequestDto request, User user, List<MultipartFile> fileList) {
        Newsfeed newsfeed = findNewsfeed(newsfeedSeq,user);
        List<String> fileUrlList = new ArrayList<>();
        Ott ott = findOtt(request);
        if (!isRemainMembersValid(ott, request.getRemainMember())) {
            throw new CustomException(ErrorCode.NEWSFEED_REMAIN_MEMBER_OVER);
        }
        newsfeed.updateNewsfeed(request,ott);
        NewsfeedResponseDto responseDto = new NewsfeedResponseDto(newsfeed);

        if(fileList != null) {
            List<NewsfeedImg> imgList = newsfeedImgRepository.findAllByNewsFeed(newsfeed);
            if(!imgList.isEmpty()) {
                if(imgList.size() >= fileList.size()) {
                    for (int i = 0; i < fileList.size(); i++) {
                        s3Service.deleteFile(imgList.get(i).getFileName());
                        imgList.get(i).updateFileName(s3Service.uploadFile(fileList.get(i), "newsfeed"));
                        fileUrlList.add(s3Service.readFile(imgList.get(i).getFileName()));
                    }
                } else if (imgList.size() < fileList.size()) {
                    int i = 0;
                    for (i = 0; i < imgList.size(); i++) {
                        s3Service.deleteFile(imgList.get(i).getFileName());
                        imgList.get(i).updateFileName(s3Service.uploadFile(fileList.get(i), "newsfeed"));
                        fileUrlList.add(s3Service.readFile(imgList.get(i).getFileName()));
                    }
                    for (int j = i; j < fileList.size(); j++) {
                        //System.out.println("j = " + j);
                        NewsfeedImg img = NewsfeedImg.builder()
                                .fileName(s3Service.uploadFile(fileList.get(j), "newsfeed"))
                                .newsFeed(newsfeed)
                                .build();
                        fileUrlList.add(s3Service.readFile(img.getFileName()));
                    }
                }
            } else {
                List<String> fileNameList = s3Service.uploadFileList(fileList);
                for(String fileName : fileNameList) {
                    NewsfeedImg img = NewsfeedImg.builder()
                            .fileName(fileName)
                            .newsFeed(newsfeed)
                            .build();
                    newsfeedImgRepository.save(img);
                    fileUrlList.add(s3Service.readFile(img.getFileName()));
                }
            }
            responseDto.setFileUrlList(fileUrlList);
        }
        return responseDto;
    }

    public void deleteNewsFeed(Long newsfeedSeq, User user) {
        Newsfeed newsfeed = findNewsfeed(newsfeedSeq, user);
        newsfeedRespository.delete(newsfeed);
    }

    //조회는 id 값만 존재하다면 user 상관없이 조회되어야함.
    public Newsfeed findNewsfeed(Long newsfeedSeq) {
        return newsfeedRespository.findById(newsfeedSeq)
                .orElseThrow(() -> new CustomException(ErrorCode.NEWSFEED_NOT_FOUND));

    }

    //삭제, 수정에 필요한 newsfeed 가져오는 메서드
    private Newsfeed findNewsfeed(Long newsfeedSeq, User user) {
        if (!newsfeedRespository.existsById(newsfeedSeq)) {
            throw new CustomException(ErrorCode.NEWSFEED_NOT_FOUND);
        }
        return newsfeedRespository.findByNewsFeedSeqAndUser(newsfeedSeq, user)
                .orElseThrow(() -> new CustomException(ErrorCode.NEWSFEED_NOT_USER));
    }

    private Ott findOtt(NewsfeedRequestDto request) {
        return ottRepository.findByOttName(request.getOttName()).
                orElseThrow(()-> new CustomException(ErrorCode.OTT_NOT_FOUND));
    }

    private boolean isRemainMembersValid(Ott ott, int remainMembers) {
        return remainMembers <= ott.getMaxMember();
    }

}
