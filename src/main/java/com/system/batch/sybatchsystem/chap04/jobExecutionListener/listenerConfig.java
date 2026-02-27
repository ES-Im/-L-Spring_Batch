package com.system.batch.sybatchsystem.chap04.jobExecutionListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class listenerConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MyAnnotationListener myAnnotationListener;

    @Bean
    public Job listenerJob() {
        return new JobBuilder("listenerJob", jobRepository)
                .start(simpleStep1())
                .listener(myAnnotationListener)
                .build();
    }


    @Bean
    public ItemReader<String> menuReader1() {
        return new ListItemReader<>(Arrays.asList(
                "ice americano", "latte", "mocha", "cappuccino", "espresso"
        ));
    }

    @Bean
    public ItemProcessor<String, String> menuProcessor1() {  // 메뉴이름 소문자(I: String)를 대문자(O:String)로 변환
        return item -> item.toUpperCase();
    }

    @Bean
    public ItemWriter<String> menuWriter1() {
        return items -> {
            log.info("-- 청크 쓰기 시작 -- ");
            for (String item : items) {
                log.info("결과 : {}", item);
            }
            log.info("-- 청크 쓰기 완료 -- ");
        };
    }

    @Bean
    public Step simpleStep1() {
        return new StepBuilder("simpleStep1", jobRepository)
                .<String, String>chunk(2)   //
                .reader(menuReader1())
                .processor(menuProcessor1())
                .writer(menuWriter1())
                .build();
    }


}
