package com.system.batch.sybatchsystem.chap02;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.job.Job;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CafeJobConfig {

    private final JobRepository jobRepository;  // 기록원
    private final PlatformTransactionManager transactionManager;    // DB관련 트랜잭션 매니저

    /*
     * Job 구성
     * 1. 카페 문 열기 - openCafeStep
     * 2. 커피 만들기(5잔만들면 퇴근) - makeCoffeeStep
     * 3. 마감 청소 및 퇴근 - closeCafeStep
     */

    private int coffeeCount = 0;
    private final int ORDER_TARGET = 5;


    // 1. step 만들기
    @Bean   // 모든 job과 step은 @Bean으로 등록해야한다.
    public Step openCafeStep() {
        return new StepBuilder("openCafeStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {  // 방식 선택 : tasklet 또는 chunk 선택
                        // 여기에 작업로직
                        log.info("[OPEN] 카페 문을 열고 머신을 예열 합니다.");
                        return RepeatStatus.FINISHED;   // 해당 step을 종료
                }, transactionManager).build();
    }

    @Bean
    public Step makeCoffeeStep() {
        return new StepBuilder("makeCoffeeStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    coffeeCount++;
                    log.info("[제조] 아메리카노 {}잔 째 완성", coffeeCount);

                    if(coffeeCount < ORDER_TARGET) {
                        return RepeatStatus.CONTINUABLE;
                    } else {
                        log.info("[완료] {}잔 완성 마무리", coffeeCount);
                        return  RepeatStatus.FINISHED;
                    }
                }, transactionManager).build();
    }

    @Bean
    public Step closeCafeStep() {
        return new StepBuilder("closeCafeStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[마감] 청소 및 문닫기");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    // 2. Job만들기
    @Bean
    public Job cafeJob() {
        return new JobBuilder("cafeJob", jobRepository)
                .start(openCafeStep())      // 시작 step
                .next(makeCoffeeStep())     // 다음 스텝
                .next(closeCafeStep())
                .build();
    }


    // 3. 배치 실행 .\gradlew bootRun --args="--spring.batch.job.name=cafeJob"
    // <- 터미널 한글 깨지면 제어판 - 시계및 국가 - 날짜, 시간 또는 숫자 형식변경 - 관리자 옵션 - 시스템 로캘 변경 - 세계언어지원을 위해 UTF-8사용 체크


}
