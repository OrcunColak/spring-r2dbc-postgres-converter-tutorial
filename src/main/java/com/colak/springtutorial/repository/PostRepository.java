package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;

import java.util.UUID;

interface PostRepository extends R2dbcRepository<Post, UUID>, ReactiveQueryByExampleExecutor<Post> {

    public Flux<PostSummary> findByTitleLike(String title, Pageable pageable);
}
