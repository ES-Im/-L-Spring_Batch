package com.system.batch.sybatchsystem.chap03;

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
public class DatePrintJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DatePrintTasklet datePrintTasklet;


    @Bean
    public Job DatePrintJob() {
        return new JobBuilder("datePrintJob", jobRepository)
                .start(datePrintStep()).build();
    }

    @Bean
    public Step datePrintStep() {
        return new StepBuilder("datePrintStep", jobRepository)
                .tasklet(datePrintTasklet, transactionManager).build();
    }
}
