package com.sparta.newspeed.newsfeed.repository;

import com.sparta.newspeed.newsfeed.entity.Newsfeed;
import com.sparta.newspeed.newsfeed.entity.NewsfeedImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsfeedImgRepository extends JpaRepository<NewsfeedImg, Long> {
    List<NewsfeedImg> findAllByNewsFeed(Newsfeed newsfeed);
}
