package com.sparta.newspeed.newsfeed.controller;

import com.sparta.newspeed.newsfeed.dto.NewsfeedRequestDto;
import com.sparta.newspeed.newsfeed.dto.NewsfeedResponseDto;
import com.sparta.newspeed.newsfeed.entity.NewsfeedImg;
import com.sparta.newspeed.newsfeed.service.NewsfeedService;
import com.sparta.newspeed.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Newsfeed API", description = "Newsfeed API 입니다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsfeeds")
public class NewsfeedController {

    private final NewsfeedService newsfeedService;

    @Operation(summary = "createNewsFeed", description = "뉴스피드 생성 기능입니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsfeedResponseDto> createNewsFeed(@Valid @RequestPart NewsfeedRequestDto requestDto,
                                                              @RequestPart(required = false) List<MultipartFile> fileList,
                                                              @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.status(HttpStatus.CREATED).body(newsfeedService.createNewsFeed(requestDto, fileList, userDetails.getUser()));
    }

    @Operation(summary = "getNewsfeeds", description = "뉴스피드 전체조회 기능입니다.")
    @GetMapping
    private ResponseEntity<List<NewsfeedResponseDto>> getNewsfeeds(){
        return ResponseEntity.ok().body(newsfeedService.getNewsfeeds());
    }

    @Operation(summary = "getNewsfeed", description = "뉴스피드 선택조회 기능입니다.")
    @GetMapping("/{newsfeedSeq}")
    private ResponseEntity<NewsfeedResponseDto> getNewsfeed(@PathVariable("newsfeedSeq") Long newsfeedSeq){
        return ResponseEntity.ok().body(newsfeedService.getNewsfeed(newsfeedSeq));
    }

    @Operation(summary = "updateNewsfeed", description = "뉴스피드 수정 기능입니다.")
    @PutMapping("/{newsfeedSeq}")
    private ResponseEntity<NewsfeedResponseDto> updateNewsfeed(@PathVariable("newsfeedSeq") Long newsfeedSeq, @Valid @RequestBody NewsfeedRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok().body(newsfeedService.updateNewsFeed(newsfeedSeq,requestDto,userDetails.getUser()));
    }

    @Operation(summary = "deleteNewsfeed", description = "뉴스피드 삭제 기능입니다.")
    @DeleteMapping("/{newsfeedSeq}")
    private ResponseEntity<String> deleteNewsfeed(@PathVariable("newsfeedSeq") Long newsfeedSeq, @AuthenticationPrincipal UserDetailsImpl userDetails){
        newsfeedService.deleteNewsFeed(newsfeedSeq,userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

}
