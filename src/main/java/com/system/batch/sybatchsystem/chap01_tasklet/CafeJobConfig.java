package com.system.batch.sybatchsystem.chap01;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CafeJobConfig {

    private final JobRepository jobRepository;  // 기록원
    private final PlatformTransactionManager transactionManager;    // DB관련 트랜잭션 매니저

    private int cakeCount = 0;
    private final int ORDER_TARGET = 10;

    @Bean
    public Step makeCakeStep() {    // tasklet 구현체 CafeJobTasklet와 기능이 같음
        return new StepBuilder("makeCakeStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    cakeCount++;

                    if(cakeCount >= ORDER_TARGET) {
                        log.info("목표 수량 완료 가게오픈");
                        return RepeatStatus.FINISHED;
                    }

                    return RepeatStatus.CONTINUABLE;
                }, new ResourcelessTransactionManager())    // 껍데기 트랜잭션 매니저
                .build();
    }

    @Bean
    public Job cakeJob() {
        return new JobBuilder("cakeJob", jobRepository)
                .start(makeCakeStep())
                .build();
    }
}
