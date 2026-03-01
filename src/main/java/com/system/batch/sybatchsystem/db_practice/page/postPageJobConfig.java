package com.system.batch.sybatchsystem.db_practice.page;

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
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class postPageJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    public Job postPageJob() throws Exception {
        return new JobBuilder("postPageJob1", jobRepository)
                .start(postPageStep()).build();
    }

    @Bean
    public Step postPageStep() throws Exception {
        return new StepBuilder("postPageStep", jobRepository)
                .<Post, Post>chunk(3)
                .reader(postPageReader())
                .processor(postPageProcessor())
                .writer(postPageWriter())
                .allowStartIfComplete(true)
                .build();
    }

    /*
     * read : JdbcPagingItemReader<?>
     * process : ItemProcessor<?, ?>
     * write : jdbcBatchItemWriter<?>
     */

    @Bean
    public JdbcPagingItemReader<Post> postPageReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<Post>()
                .name("postPageReader")
                .dataSource(dataSource)
                // 한 번에 조회할 데이터의 최대 개수, 즉, 쿼리 기준 LIMIT절에 들어갈 값 지정 청크랑 같은 사이즈로 맞추어야 헷갈리지 않음.
                .pageSize(3)
                // 쿼리 동적 세팅
                .selectClause("select *")
                .fromClause("from posts")
                .whereClause("id <= :id")
                .sortKeys(Map.of("id", Order.ASCENDING))
                .parameterValues(Map.of("id", 9))
                .beanRowMapper(Post.class)
                .build();
    }

    @Bean
    public ItemProcessor<Post, Post> postPageProcessor() {
        return item -> {
            boolean isEven = item.getId() % 2 == 0;
            return isEven ? item : null;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Post> postPageWriter() {
        return new JdbcBatchItemWriterBuilder<Post>()
                .dataSource(dataSource)
                .sql("""
				UPDATE posts
				SET views = views + 5,
					updated_at = NOW()
				WHERE id = :id
				""")
                .beanMapped()
                .assertUpdates(true)
                .build();
    }
}
