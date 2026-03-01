# DB 배치 작업 처리방식
1. 커서 기반
2. 페이징 기반

# 커서 기반 처리 
 - 스트리밍 방식과 유사
 - 하나의 DB 커넥션에서 데이터를 순차적으로 불러오며 정의된 배치 작업을 수행
 - 즉, DB 커넥션을 길게 가져가야 한다는 단점 존재

## 커서 기반 : 데이터를 조회하는 방식(`ORDER BY`)
 - 데이터를 '버퍼'에 여러 개를 미리 불러오지만, 처리는 하나씩 꺼내서 처리
 - 조회 쿼리에 `Order by`절을 포함시켜야한다.
 - `Order by`가 없다면 **데이터를 조회할 때마다** 데이터의 순서가 바뀌면 누락 및 중복이 생김

### Cursor 기반 예시 - `PostCursorConfig.java`
#### 1. `PostCursorReader` - JDBC
```java
    @Bean
    public JdbcCursorItemReader<Post> postCursorReader() {
        return new JdbcCursorItemReaderBuilder<Post>()
                .name("postCursorReader")
                .dataSource(dataSource)     // 어떤 DB에 연결할 건지결정하는 DataSource 지정, 여기서 얻은 커넥션으로 쿼리를 실행
                .sql("SELECT * FROM posts WHERE id <= ? ORDER BY ID ASC")
                .queryArguments(List.of(9))     // sql파라미터 바인딩을 채워준다. 이 대신 @Value와 @StepScope로 대체할 수 있음
                .beanRowMapper(Post.class)  // ResultSet의 한 행(row)을 Post 객체로 바꾸는 RowMapper 설정.
//                .rowMapper(((rs, rowNum) -> {}))  // 컬럼과 자바 필드/setter가 불일치하면 여기서 매칭시키면 된다.
                .build();
    }
```
> 쿼리 결과를 Java 객체와 맵핑해주는 것은 .beanRowMapper() 또는 .rowMapper()로 구현할 수 있는데, .beanRowMapper()는 Setter가 모두 정의되어 있어야 정상적으로 동작하며, 커스텀한 맵핑 코드를 구현하고 싶다면 .rowMapper()에 맵핑 코드를 작성해주면 된다.

▶ 도메인라인에 setter를 심어둘 순 없으니, 실제 프로젝트에 적용할 때에는 **도메인 - 배치 사이에 record**를 심어두면 된다.
```java
// record
public record PostRow(Long id, String title, String content) {}

// RowMapper
.rowMapper((rs, rowNum) -> new PostRow(
        rs.getLong("id"),
    rs.getString("title"),
    rs.getString("content")
))
```
#### 2. `PostJpaCursorConfig.java` - JPA


# 페이지 기반 처리
 - 데이터를 정해진 페이지 사이즈만큼 조회하고, 메모리에 올려서 처리하는 방식
 - 메모리를 많이 사용하지만, 각 페이지 별로 구분되어 처리될 수 있어 명확한 처리 가능
 - 스프링 배치에서 사용되는 `KeySet`방식과 인피니트 스크롤링으로 동작하는 `Offset` 쿼리 유형 방식으로 나뉜다.
 - 페이지 기반 처리도 `Order By`를 명시해줘야한다.

## 페이징 기반 : KeySet/Offset 기반 페이징 쿼리 예시
1) Offset 기반
```sql
SELECT * FROM posts ORDER BY id LIMIT 10 OFFSET 20;
```
2) KeySet 기반
```sql
SELECT * FROM posts WHERE id > 1000 ORDER BY id LIMIT 10;
```

### page 기반 예시 - `postPageJobConfig.java`
#### 1. `postPageReader` - JDBC
```java
@Bean
public JdbcPagingItemReader<Post> postPageReader() throws Exception {
    return new JdbcPagingItemReaderBuilder<Post>()
            .name("postPageReader")
            .dataSource(dataSource)
            
            // 한 번에 조회할 데이터의 최대 개수, 즉, 쿼리 기준 LIMIT절에 들어갈 값 지정 청크랑 같은 사이즈로 맞추어야 헷갈리지 않음.
            .pageSize(3)
            
            // 쿼리 동적 세팅
            .selectClause("select *")
            .fromClause("from posts")
            .whereClause("id <= :id")
            .sortKeys(Map.of("id", Order.ASCENDING))
            .parameterValues(Map.of("id", 9))
            
            .beanRowMapper(Post.class)
            .build();
}
```



