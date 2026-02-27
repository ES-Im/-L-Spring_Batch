# JobParameters란?
 - 왜 쓰나? 예_오래된 접속로그 삭제 배치를 할때 기준날짜는 매일 변함 -> 이때 필요한것이 JobParameters
 - 정의 : 배치 Job을 실행할 때 외부에서 주입하는 파라미터 묶음
 - 용도 : 매일 변하는 날짜, 특정 파일 경로 API요청 ID등 동적인 값을 처리할 때 사용
 - 장점 : 값을 바꾸기 위해 코드를 수정하거나 재배포할 필요가 없다.

### 사용법 
- 스프링 배치에서는 `@Value` annotation과 `@StepScope`를 조합하여 작성해야하며,
- 값을 꺼낼때는 **SpEL(EL표현식)** 을 사용해 값을 꺼낸다

### DatePrintTasklet
```java
@StepScope
@Component
@Slf4j
public class DatePrintTasklet implements Tasklet {

    private final String requestDate;

    public DatePrintTasklet(@Value("#{jobParameters['requestDate']}") String requestDate) { // 동적 파라미터를 받아오는 방법
        this.requestDate = requestDate;
    }

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("외부에서 받은 날짜 : {}", requestDate);
        log.info("{} 날짜의 데이터를 처리합니다!", requestDate);

        return RepeatStatus.FINISHED;
    }
}
```
- `@Value("#{jobParameters['파라미터']}")` : 외부에서 받은 값을 인자값으로 넣는다.

### Terminal
```terminaloutput
./gradlew bootRun --args="--spring.batch.job.name=datePrintJob requestDate=20240126"
```
- requestDate=20240126 와 같이 명령문 뒤에 jobParameter를 입력하면 파라미터 주입이 된다.

## `@StepScope` 어노테이션을 붙이는 이유
### `@StepScope`의 역할
 - spring Bean으로 등록한 대상들은 서버 켜짐과 동시에 스프링 관리 대상이 된다
 - 위 `DatePrintTasklet`의 경우 jobParameters를 받고 있기 때문에, 앱 실행 시, Bean을 생성하면 오류가 발생
 - 따라서 앱이 켜질때 만들지 말고 "파라미터"가 들어올 때까지 스프링이 기다려야함(지연된 빈 생성 - Lazy Bean Creation) 
 - lazy bean creation 역할을 해주는게 `@StepScope`