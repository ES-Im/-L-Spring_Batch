package com.system.batch.sybatchsystem.chap04;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EmailListenerConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobReportListener jobReportListener;


    @Bean
    public Job emailJob() {
        return new JobBuilder("emailJob", jobRepository)
                .start(emailStep())
                .listener(jobReportListener)
                .build();
    }

    @Bean
    public Step emailStep() {
        return new StepBuilder("emailStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("3초 뒤 에러를 발생");
                    Thread.sleep(3000);
                    throw new RuntimeException("테스트 오류");
                }, transactionManager).build();
    }
}
