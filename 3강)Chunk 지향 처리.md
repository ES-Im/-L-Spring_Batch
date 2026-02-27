# CHUNK 지향처리
 - 청크 : 크고 무거운 데이터의 덩어리
 - 데이터 100만개를 처리할 때 1000개 짜리 덩어리로 잘라서 처리
 - 대용량 처리가 필요할 때 해당 방식을 사용
   - 매월 1일, 멤버십 포인트 지급
   - 배송 상태 일괄 변경
   - 구형 시스템 데이터 이관
   - 실패하더라도 현재 청크 단위부터 시작하면 된다.

## 청크 인터페이스
 - 데이터를 읽고, 가공하여, 쓰는 루틴 : *읽고 가공하는건 한건씩 처리 - 쓰는 건 청크 사이즈까지 모았다가 실행*
 - 
`package org.springframework.batch.infrastructure.item;`
1. ItemReader<T> - Null을 반환하면 종료함
```java
@FunctionalInterface
public interface ItemReader<T> {
	@Nullable T read() throws Exception;
}
```
2. ItemProcess<T, O>
```java
@FunctionalInterface
public interface ItemProcessor<I, O> {  // I : 인풋의 자료형 O : 아웃풋의 자료형
    @Nullable O process(I item) throws Exception;

}
```
3. ItemWriter<T>
```java
@FunctionalInterface
public interface ItemWriter<T> {
    void write(Chunk<? extends T> chunk) throws Exception;
}
```

## 청크 기반 배치처리 순서 
1. itemXXX -> chunk : itemReader, itemProcess, itemWriter 로 청크 설정
2. chunk -> Step : new StemBuilder 등록할때, 청크 사이즈와 각 청크 구현체 지정
3. Step -> Job

## 청크가 마지막을 판단하는 기준?
 - Tasklet은 개발자가 코드 끝에 return RepeatStatus.FINISHED; 명시로 종료
 - chunk는 개발자가 종료 코드 작성 x, itemReader의 마지막 데이터를 읽으면 null을 반환 -> Spring Batch가 "해다 ㅇ데이터가 마지막이니까 더 반복하지 말라고 표시함"
 - itemReader의 작업이 끝나면, itemProcessor와 itemWriter가 작업을 시작.
 - Spring Batch의 step은 실행 전 마지막 여부를 확인하고, 마지막이면 반복을 종료함