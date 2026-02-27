package com.system.batch.sybatchsystem.chap02_chunk;

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
public class SimpleChunkConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /*
     * 청크 목적 : 메뉴판이름을 대문자로 전부 변환
     * 청크 사이즈는 2
     */


    // 1. 청크 등록
    // itemReader - 메뉴판에서 대상 읽기 : 데이터 하나씩 읽음
    @Bean
    public ItemReader<String> menuReader() {
        return new ListItemReader<>(Arrays.asList(
                "ice americano", "latte", "mocha", "cappuccino", "espresso"
        ));
    }

    //itemProcessor - 대문자로 바꾸기 : 데이터 하나씩 처리함
    @Bean
    public ItemProcessor<String, String> menuProcessor() {  // 메뉴이름 소문자(I: String)를 대문자(O:String)로 변환
        return item -> item.toUpperCase();
    }

    // itemWriter - 실행 : 청크 사이즈 만큼 데이터 모았다가 실행함
    @Bean
    public ItemWriter<String> menuWriter() {
        return items -> {
            log.info("-- 청크 쓰기 시작 -- ");
            for (String item : items) {
                log.info("결과 : {}", item);
            }
            log.info("-- 청크 쓰기 완료 -- ");
        };
    }

    // 2. 스텝 등록 : 여기서 청크 사이즈, itemXXX을 설정
    @Bean
    public Step simpleStep() {
        return new StepBuilder("simpleStep", jobRepository)
                .<String, String>chunk(2)   //
                .reader(menuReader())
                .processor(menuProcessor())
                .writer(menuWriter())
                .build();
    }


    // 3. 잡 등록
    @Bean
    public Job simpleJob() {
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleStep()).build();
    }
}
