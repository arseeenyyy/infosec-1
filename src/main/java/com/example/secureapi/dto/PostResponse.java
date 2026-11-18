package com.example.secureapi.dto;

import com.example.secureapi.model.Post;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;

public record PostResponse(Long id, String title, String content, String author, Instant createdAt) {

    public static PostResponse from(Post p) {
        return new PostResponse(
                p.getId(),
                HtmlUtils.htmlEscape(p.getTitle()),
                HtmlUtils.htmlEscape(p.getContent()),
                HtmlUtils.htmlEscape(p.getAuthor()),
                p.getCreatedAt());
    }
}
