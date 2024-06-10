package com.sparta.newspeed.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    FAIL(500, "실패했습니다."),
    USER_NOT_FOUND(400, "해당하는 유저를 찾을 수 없습니다."),
    USER_NOT_VALID(400, "이미 탈퇴 처리된 유저입니다."),
    DUPLICATE_PASSWORD(400, "기존 비밀번호와 동일한 비밀번호입니다."),
    USER_NOT_UNIQUE(400,"중복된 사용자가 존재합니다."),
    INCORRECT_PASSWORD(400, "입력하신 비밀번호가 일치하지 않습니다."),
    NEWSFEED_NOT_FOUND(404,"뉴스피드를 찾을 수 없습니다."),
    NEWSFEED_NOT_USER(400, "해당 뉴스피드의 작성자가 아닙니다."),
    NEWSFEED_REMAIN_MEMBER_OVER(400, "남은 인원수가 전체 인원수를 초과했습니다."),
    NEWSFEED_EMPTY(200,"먼저 작성하여 소식을 알려보세요!"),
    OTT_NOT_FOUND(404,"해당 이름의 OTT 를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(404,"댓글을 찾을 수 없습니다."),
    COMMENT_NOT_USER(400, "해당 댓글의 작성자가 아닙니다."),
    TOKEN_EXPIRED(400, "토큰이 만료되었습니다."),
    TOKEN_NOT_FOUND(400, "토큰을 찾을 수 없습니다."),
    WRONG_FILE_FORMAT(400, "잘못된 파일 포맷입니다."),
    IMG_SIZE_OUTOFRANGE(400, "사진의 크기가 10mb 초과입니다."),
    VIDEO_SIZE_OUTOFRANGE(400, "동영상의 크기가 200mb 초과입니다.")
    ;
    private int status;
    private String msg;
}