# 컴포넌트 
1. job : 배치 처리의 최상위 단위
    - 매월 1일에 처리되는 '이자지급 Job', 매달 30일에 처리되는 '오래된 로그 정리 Job'
    - 할일 리스트
2. step :  Job을 구성하는 독립적인 작업 단계
    - Job을 구성하는 세부 단계, 실제 작업이 일어나는 실행단위
    - 하나의 Job은 여러 개의 step으로 구성
    - 예 ) 오래된 로그 정리 Step
        - READ : 특정 날짜 이전의 로그 데이터 조회
        - Process : 중요한 정보가 없는지 필터링 및 확인
        - Write : 선별된 로그 데이터 삭제 (또는 아카이빙) 처리
      
3. JobOperator : Job을 실행시키는 엔진 역할(실행버튼)
    - JobLauncher를 포함하여 중지/재시작 등 관리 기능까지 제공하는 인터페이스
    - 사람이 배치를 제어할 수 있도록 돕는 '운영 조작 패널'역할
    - 내부적으로는 JobLauncher를 사용하여 Job을 구동

4. JobRepository : 배치 실행 과정 (성공/실패/시간)을 DB에 기록하고 관리하는 저장소(기록원)
    - Job / Step의 시작/종료 시간, 성공/실패 여부, 읽은 데이터 수 기록
    - 배치 작업 이후 모니터링에서 해당 데이터를 활용 (성공했냐 실패했냐? 확인)

5. ExecutionContext : 배치 실행 중 공유되는 데이터 저장소 (장애 발생 시 복구의 실마리)

6. ItemReader / Processor / Writer : 데이터를 읽고 가공하고 쓰는 핵심 인터페이스

# SyBatchSystemApplication
--

```
    public static void main(String[] args) {
        // Jenkin 등 연동을 위해 변경
        System.exit(SpringApplication.exit(SpringApplication.run(SyBatchSystemApplication.class, args)));
    }
```
# CafeJobConfig.java
--

- job, step 빈 생성법
- 

```
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

```



출처 : [Spring Batch 입문 3시간 만에 끝내는 대용량 처리의 기초](https://www.youtube.com/playlist?list=PLtUgHNmvcs6oBv-Edp_jD4KZY76wT2x4X)