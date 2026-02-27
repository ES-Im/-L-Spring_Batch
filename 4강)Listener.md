# 리스너 : 배치의 CCTV
### Listener란? 
 - 정의
   - 배치 작업은 백그라운드에서 실행 -> 눈으로 확인하기 어려움
   - 리스너는 배치의 주요 시점(시작전, 종료 후)를 감지하고 우리가 원하는 로직을 끼워 넣을 수 있게 해주는 인터페이스
 - 역할
   - 모니터링 : 배치의 시작/종료, 성공/실패 를 기록
   - 알림 발송 : 배치가 실패하면 문자 / 슬랙 등 발송
   - 데이터 초기화 : 작업 시작 전 필요한 폴더를 만들거나 변수 초기화

### 리스너 종류
1. JobExecutionListener : Job 전체의 lifecycle 감시
2. StepExecutionListener : 각 Step의 내부 동작 감시

#### JobExecutionListener
- Job 실행의 시작과 종료 시점에 호출되는 리스너 인터페이스
- `beforeJob` : Job 시작 직전에 실행 (리소스 준비 등 초기화 작업)
- `afterJob` : Job 종료 직 후 실행 (리소스 정리, 메일보고, Job상태 변경 등 후처리 작업)

#### StepExecutionListener
- Step 실행의 시작과 종료 시점에 호출되는 리스너 인터페이스
- `beforeStep` : Step 시작 직전 실행
  - `StepExecution` 객체를 통해 Step의 메타 데이터에 접근 가능
  - Step 실행 전 필요한 사전 검증 수행 (예시 - 파일 존재 체크)
- `afterStep` : Step 종료 직후 실행
  -  ExitStatus(반환값) 조작 가능 : Step의 성공/실패 상태를 조건에 따라 변경 가능 
    - 예 : 처리건수가 0건이면 실패로 처리
  - 통계 확인 : `getReadCount()`, `getWriteCount()`등을 통해 몇 건이 처리 됐는지 로그 기록


#### Listener 만드는 방법
1. 인터페이스로 만들기
```java
@Slf4j
@Component
public class MyJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[Job 시작] id : {}", jobExecution.getJobInstanceId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.FAILED) {
            log.warn("[Job 실패] 관리자에게 연락");
        } else {
            log.info("[Job 종료] 정상적으로 배치 실행");
        }
    }
}
```

2. 어노테이션으로 만들기
```java
@Component
@Slf4j
public class MyAnnotationListener {

    @BeforeJob
    public void announceStart(JobExecution jobExecution) {
        log.info("[job 시작] id: {}", jobExecution.getJobInstanceId());
    }

    @AfterJob
    public void announceEnd(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.FAILED) {
            log.warn("[Job 실패] : 관리자에게 연락");
        } else {
            log.info("[Job 성공] : Job 성공");
        }
    }
}
```

#### Config에 Listener 등록 (annotation, interface 방식 둘 다 같음)
```java
    private final MyAnnotationListener myAnnotationListener;

    @Bean
    public Job listenerJob() {
        return new JobBuilder("listenerJob", jobRepository)
                .start(simpleStep1())
                .listener(myAnnotationListener)     // 여기에 등록
                .build();
    }
```