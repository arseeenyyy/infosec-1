package com.example.secureapi.controller;

import com.example.secureapi.dto.PostRequest;
import com.example.secureapi.dto.PostResponse;
import com.example.secureapi.model.Post;
import com.example.secureapi.repository.PostRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {

    private final PostRepository posts;

    public DataController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("/data")
    public List<PostResponse> data(@RequestParam(required = false) String search) {
        List<Post> result = (search == null || search.isBlank())
                ? posts.findAllByOrderByCreatedAtDesc()
                : posts.search(search);
        return result.stream().map(PostResponse::from).toList();
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody PostRequest req, Authentication auth) {
        Post saved = posts.save(new Post(req.title(), req.content(), auth.getName()));
        return PostResponse.from(saved);
    }
}
