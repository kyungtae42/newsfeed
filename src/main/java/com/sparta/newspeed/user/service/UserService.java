package com.sparta.newspeed.user.service;

import com.sparta.newspeed.awss3.S3Service;
import com.sparta.newspeed.common.exception.CustomException;
import com.sparta.newspeed.common.exception.ErrorCode;
import com.sparta.newspeed.user.dto.UserInfoUpdateDto;
import com.sparta.newspeed.user.dto.UserPwRequestDto;
import com.sparta.newspeed.user.dto.UserResponseDto;
import com.sparta.newspeed.user.dto.UserStatusDto;
import com.sparta.newspeed.user.entity.User;
import com.sparta.newspeed.user.entity.UserRoleEnum;
import com.sparta.newspeed.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, S3Service s3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
    }


    public UserResponseDto getUser(Long userSeq) {
        User user = findById(userSeq);
        UserResponseDto responseDto = new UserResponseDto(user);
        responseDto.setPhotoUrl(s3Service.readFile(user.getPhotoName()));
        return responseDto;
    }

    public User findById(Long userSeq) {
        return userRepository.findById(userSeq).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Transactional
    public UserInfoUpdateDto updateUser(Long userSeq, UserInfoUpdateDto requestDto, MultipartFile file) {
        User user = findById(userSeq);
        if (file != null) {
            if(user.getPhotoName() != null) { s3Service.deleteFile(user.getPhotoName()); }
            user.setPhotoName(s3Service.uploadFile(file, "profile"));
        }
        user.updateUserInfo(requestDto);
        return new UserInfoUpdateDto(user);
    }

    @Transactional
    public void updateUserPassword(Long userSeq, UserPwRequestDto requestDto) {
        User user = findById(userSeq);
        if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.DUPLICATE_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    @Transactional
    public void updateWithdraw(Long userSeq, UserStatusDto requestDto) {
        User user = findById(userSeq);

        // ID가 일치하지 않을 경우
        if(!requestDto.getUserId().equals(user.getUserId())){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        // PW가 일치하지 않을 경우
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }
        // 회원이 이미 탈퇴 상태일 경우
        if (user.getRole().equals(UserRoleEnum.WITHDRAW)){
            throw new CustomException(ErrorCode.USER_NOT_VALID);
        }

        // 회원 상태를 탈퇴로 변경
        user.updateRole(UserRoleEnum.WITHDRAW);
    }
}
