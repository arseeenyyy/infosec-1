package com.example.secureapi.repository;

import com.example.secureapi.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    @Query("select p from Post p where lower(p.title) like lower(concat('%', :q, '%')) order by p.createdAt desc")
    List<Post> search(@Param("q") String q);
}
