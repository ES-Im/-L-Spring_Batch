# Tasklet 지향처리란?
 - Tasklet : 작고 단순한 작업 하나
 - 단순하고 독립적인 작업을 처리할 때 사용하는 방식
 - 단발성 작업이 필요할 때 주로 사용
   - 파일 관리
   - 단순 DB 작업
   - 이메일 혹은 문자 알림 발송
   - 쉘 스크립트 실행

## Tasklet 인터페이스
```
    @FunctionalInterface
    public interface Tasklet {
    
        @Nullable RepeatStatus execute(StepContribution contribution
                                      , ChunkContext chunkContext) throws Exception;
    
    }
```

1. return Type : RepeatStatus
    - RepeatStatus.FINISHED : 끝
    - RepeatStatus.CONTINUABLE : 계속, 작업을 다시 실행
2. StepContribution
    - 현재 단계(step)이 얼마나 진행되었는지를 기록하는 객체
    - Read cnt, Write cont 등 전체 x개 중 t개 완료 같은 _상태_ 정보
3. ChunkContext
    - 작업 수행에 필요한 정보가 담겨있는 객체
    - 외부에서 넘겨준 파라미터(JobParameters), 이전 단계에서 넘겨준 데이터(ExecutionContext)

# Tasklet 인터페이스로 단일 배치 작업 처리하기

## CafeJobTasklet.java
 - Tasklet을 구현해서 Step을 구현하는 것 1강의 StepBuilder.tasklet(..람다식..)으로 구현한 것과 같은 기능을 한다.
```java
    @Slf4j
    @Component
    public class CafeJobTasklet implements Tasklet {
    
        private int cakeCount = 0;
        private final int ORDER_TARGET = 10;
    
        @Override
        public @Nullable RepeatStatus execute(StepContribution contribution,
                                              ChunkContext chunkContext) throws Exception {
            cakeCount++;
            log.info("케이크를 만들고 있습니다. ({}/{})", cakeCount, ORDER_TARGET);
            if(cakeCount >= ORDER_TARGET) {
                log.info("목표 수량 완료, 가게 오픈");
                return RepeatStatus.FINISHED;
            }
    
            return RepeatStatus.CONTINUABLE;
        }
    
    }
```

## while문으로 한 번에 처리하면 안되는 이유?
 - ? "Tasklet"은 소량의 작업 처리인데, 그냥 while문으로 한번에 처리하면 안되는지?
 - ! 소량의 데이터더라도 tasklet이 아닌 while문으로 한번에 처리하면 문제점이 while전체가 하나의 트랜잭션으로 돌아가서 마지막에 에러걸리면 전체 롤백된다.
 - ! tasklet을 사용하면 execute가 실행될때마다 트랜잭션이 새로 실행이 된다 -> 만약 해당 트랜잭션(tasklet)에서 에러가 나면 이미 완료된 tasklet은 롤백을 안해도 된다

## 트랜잭션 처리가 불필요한 배치는 어떻게 만들까?
 - tasklet 배치 방식은 간단하게 처리되는 경우가 많아 DB의 트랜잭션 관리가 불필요한 경우가 있음
 - 그러므로 transactionManager를 넘길필요가 없음
 - 이때는 PlatformTransactionManager말고 껍데기를 쓰면 된다.

### ResourcelessTransactionManager [CafeJobConfig.java]
 - Resourceless + transaction + manager 즉, DB와 연결되지 않고 껍데기만 있는 트랜잭션 매니저(DB의 커밋, 롤백 수행 x)
 - 예 ) DB가 필요없는 단순 파일 처리, 단순 API 호출, 테스트 코드 작성

# 오래된 접속 로그 자동 삭제하는 기능 구현하기
 - 대상 테스트 클래스 : LogGenerator.java
 1. 로그를 삭제하는  tasklet 구현
 2. 배치 Config 파일에서 tasklet 빈등록 - step 빈 등록 - job 빈 등록
    - tasklet 구현체 : FileCleanupTasklet.java
    - 배치 Config 파일 : FileCleanupConfig