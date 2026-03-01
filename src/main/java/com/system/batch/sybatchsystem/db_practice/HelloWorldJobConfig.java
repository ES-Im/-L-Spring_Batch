package com.system.batch.sybatchsystem.db_practice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloWorldJobConfig {

    private final JdbcTemplate jdbcTemplate;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job helloWorldJob(Step helloWorldStep) {
        return new JobBuilder("helloWorldJob", jobRepository)
                .start(helloWorldStep)
                .build();
    }

    @Bean
    public Step helloWorldStep() {
        return new StepBuilder("helloStep", jobRepository)
                .tasklet(helloWorldTasklet(), transactionManager)
                .allowStartIfComplete(true)
                .build();

    }

    @Bean
    public Tasklet helloWorldTasklet() {
        return ((contribution, chunkContext) -> {
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION",
                    Long.class
            );
            log.info("Hollow world! {}", ids.size());
            return RepeatStatus.FINISHED;
        });
    }



}
