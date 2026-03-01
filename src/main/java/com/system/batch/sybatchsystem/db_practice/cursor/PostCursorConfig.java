package com.system.batch.sybatchsystem.db_practice.cursor;

import com.system.batch.sybatchsystem.db_practice.domain.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PostCursorConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;    // JDBC, JPA, Hibernate 등 특정 기술에 종속되지 않고 일관된 방식으로 트랜잭션을 시작 커밋 롤백하는 추상화된 역할
    private final DataSource dataSource;    // DB 커넥션 관리

    @Bean
    public Job postCursorJob() {
        return new JobBuilder("postCursorJob", jobRepository)
                .start(postCursorStep()).build();
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
    /*
     * read = JdbcCursorItemReader<?>
     * process = ItemProcessor<?, ?>
     * Write = JdbcBatchItemWriter<?>
     */

    @Bean
    public JdbcCursorItemReader<Post> postCursorReader() {
        return new JdbcCursorItemReaderBuilder<Post>()
                .name("postCursorReader")
                .dataSource(dataSource)     // 어떤 DB에 연결할 건지결정하는 DataSource 지정, 여기서 얻은 커넥션으로 쿼리를 실행
                .sql("SELECT * FROM posts WHERE id <= ? ORDER BY ID ASC")
                .queryArguments(List.of(9))     // sql파라미터 바인딩을 채워준다. 이 대신 @Value와 @StepScope로 대체할 수 있음
                .beanRowMapper(Post.class)  // ResultSet의 한 행(row)을 Post 객체로 바꾸는 RowMapper 설정.
//                .rowMapper(((rs, rowNum) -> {}))  // 컬럼과 자바 필드/setter가 불일치하면 여기서 매칭시키면 된다.
                .build();
    }

    @Bean
    public ItemProcessor<Post, Post> postCursorProcessor() {
        return item -> {
            boolean isEven = item.getId() % 2 == 0;
            return isEven ? item : null;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Post> postWriter() {
        return new JdbcBatchItemWriterBuilder<Post>()
                .dataSource(dataSource)
                .sql("""
                        UPDATE posts
                        SET
                            views = views + 1,
                            updated_at = NOW()
                        WHERE id = :id
                        """)
                .beanMapped()
                .assertUpdates(true)
                .build();
    }


}
