package com.system.batch.sybatchsystem.chap01_tasklet.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FileCleanupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Tasklet fileCleanupTasklet() {
        return new FileCleanupTasklet("./test-logs", 30);
    }

    @Bean
    public Step fileCleanupStep() {
        return new StepBuilder("fileCleanupStep", jobRepository)
                .tasklet(fileCleanupTasklet(), transactionManager).build();
    }

    @Bean
    public Job fileCleanupJob() {
        return new JobBuilder("fileCleanupJob", jobRepository)
                .start(fileCleanupStep()).build();
    }
}
