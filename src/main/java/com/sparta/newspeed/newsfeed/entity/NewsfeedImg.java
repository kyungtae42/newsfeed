package com.sparta.newspeed.newsfeed.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsfeedImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsfeedimg_seq")
    private Long newsFeedImgSeq;

    @NotBlank
    @Column(name="file_name", nullable = false)
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "newsfeed_seq")
    private Newsfeed newsFeed;
}
