package com.sparta.newspeed.newsfeed.entity;

import com.sparta.newspeed.common.Timestamped;
import com.sparta.newspeed.newsfeed.dto.NewsfeedRequestDto;
import com.sparta.newspeed.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "newsfeed")
public class Newsfeed extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "newsfeed_seq")
    private Long newsFeedSeq;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "remain_member")
    private int remainMember;

    @ManyToOne
    @JoinColumn(name = "user_seq")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ott_seq")
    private Ott ott;

    @OneToMany(mappedBy = "newsFeed", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    List<NewsfeedImg> imgList;

    public void updateNewsfeed(NewsfeedRequestDto request,Ott ott) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.remainMember = request.getRemainMember();
        this.ott = ott;
    }
}
