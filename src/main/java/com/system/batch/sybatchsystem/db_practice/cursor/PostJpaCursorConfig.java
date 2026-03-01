package com.system.batch.sybatchsystem.db_practice.cursor;

import com.system.batch.sybatchsystem.db_practice.domain.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PostJpaCursorConfig {

    private final JobRepository  jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;    // JPA 작업을 위한 EntityManager 생산 공장(설정/커넥션 정보 포함).
    @PersistenceContext
    private EntityManager entityManager;    // 현재 트랜잭션 범위에서 실제 DB 작업을 수행하는 JPA 세션.

    @Bean
    public Job postCursorJob() {
        return new JobBuilder("postCursorJob", jobRepository)
                .start(postCursorStep())
                .build();
    }

    @Bean
    public Step postCursorStep() {
        return new StepBuilder("postCursorStep", jobRepository)
                .<Post, Post>chunk(3)
                .reader(postCursorReader())
                .processor(postCursorProcessor())
                .writer(postWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public JpaCursorItemReader<Post> postCursorReader() {   // JPA 구현
        return new JpaCursorItemReaderBuilder<Post>()
                .name("postCursorReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                        SELECT p FROM Post p
                        WHERE p.id <= :id
                        ORDER BY p.id ASC
                        """)
                .parameterValues(Map.of(
                        "id", 9
                ))
                .build();
    }

    @Bean
    public ItemProcessor<Post, Post> postCursorProcessor() {    // jdbc와 동일
        return item -> {
            boolean isEven = item.getId() % 2 == 0;
            return isEven ? item : null;
        };
    }

    @Bean
    ItemWriter<Post> postWriter() { // JPA 구형
        return item -> item.forEach(p -> {
            log.info("postWriter: {}", p);
            Post post = entityManager.merge(p);
            post.addViews();
        });
    }



}
