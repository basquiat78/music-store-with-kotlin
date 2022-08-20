# 뮤지션의 앨범을 생성해 보자

이제는 뮤지션의 음반을 생성해 보자.

그러기 위해서는 생성할 앨범 정보를 담을 vo를 만든다.

```Kotlin
/**
 * 앨범 생성 및 변경을 위한 request 객체
 */
data class AlbumRequest(

    val title: String,
    val rep: YesOrNo? = YesOrNo.N,
    val albumType: AlbumType? = AlbumType.FULL,
    val musicianId: Long? = null,
    val id: Long? = null,

    )
```

서비스 레이어에서 해당 요청 정보를 처리하는 로직을 추가한다.


```Kotlin
/**
 * 뮤지션 정보를 db에 생성한다.
 * @return AlbumDto
 */
@Transactional
fun createAlbum(request: AlbumRequest): AlbumDto {
    val musicianId = request.musicianId ?: mandatoryParam("뮤지션 아이디는 필수 입니다.")
    val targetMusician = musicianService.findMusicianById(musicianId)
    val createAlbum = with(request){Album(musician = targetMusician, title = title, albumType = albumType, rep = rep )}
    albumRepository.save(createAlbum)
    return AlbumDto.createWithMusician(createAlbum, MusicianDto.create(targetMusician))
}
```
로직 자체는 이전 뮤지션과 별반 다를게 없다.

단 지금은 단방향이기 때문에 해당 앨범의 뮤지션 정보를 가져오고 Album엔티티에 세팅을 해줘야 한다.

양방향과 편의 메소드를 통해서 추가할 뮤지션의 정보를 가져와 앨범 리스트를 변경하는 방법도 가능하지만 여기서는 그렇게 하지 않을 생각이다.


```Kotlin
@Test
@DisplayName("앨범을 생성한다.")
fun saveAlbum_TEST() {
    // John Coltrane의 음반 Ballads를 생성해보자.
    val request = AlbumRequest(musicianId = 2, title = "Ballads", rep = YesOrNo.Y)
    val createAlbum = albumService.createAlbum(request)
    with(createAlbum) {
        assertThat(musician.name).isEqualTo("John Coltrane")
        assertThat(title).isEqualTo("Ballads")
    }

}
```
@Transactional을 테스트 코드에 붙여주면 테스트이후 롤백이 될 것이지만 나의 경우에는 그냥 데이터를 밀어 넣을 생각이다.


이제는 컨트롤러를 만들어 실제 API에서 호출해 보자.

뮤지션의 생성이후 리턴을 따로 하지 않았지만 여기서는 리턴을 해볼 생각이다.

그리기 위해서는

```Kotlin
/**
 * 앨범 정보를 db에 생성한다.
 * @return AlbumDto
 */
@Transactional
fun createAlbum(request: AlbumRequest): AlbumDto {
    val musicianId = request.musicianId ?: mandatoryParam("뮤지션 아이디는 필수 입니다.")
    val targetMusician = musicianService.findMusicianById(musicianId)
    val createAlbum = with(request){Album(musician = targetMusician, title = title, albumType = albumType, rep = rep )}
    albumRepository.save(createAlbum)
    return AlbumDto.createWithMusician(createAlbum, MusicianDto.create(targetMusician))
}
```
AlbumDto로 반환하게 만들고

```Kotlin
/**
 * 앨범 정보를 생성한다.
 * @param request: AlbumRequest
 * @return ResponseResult<AlbumDto>
 */
@PostMapping("/albums")
fun createAlbum(@RequestBody request: AlbumRequest) = ResponseResult.of(albumService.createAlbum(request))
```
처럼 만들어서 호출을 해보자.

```json
// -POST localhost:8081/api/music/store/albums
// THelonious Monk의 대표 음반 'Round Midnight, albumType은 졍규 음반이니 비워도 상관없다.

{
  "title" : "'Round Midnight",
  "musicianId": 4,
  "rep":"Y"
}

// response

{
  "result": {
    "id": 7,
    "musicianName": "Thelonious Monk",
    "title": "'Round Midnight",
    "albumType": "정규음반",
    "representative": "Y"
  }
}

```

# 앨범 정보를 변경해 보자.

단순하다.

음반 아이디로 조회한 이후 오타가 난 음반 타이틀을 수정해 보자.


```Kotlin
/**
 * 앨범 정보를 변경한다.
 */
@Transactional
fun updateAlbum(request: AlbumRequest): AlbumDto {
    val albumId = request.id ?: mandatoryParam("앨범 아이디는 필수 입니다.")
    val updateAlbum = albumRepository.findByIdOrThrow(albumId)
    updateAlbum.title = request.title
    return AlbumDto.create(updateAlbum)
}
```

테스트도 충실하게 해보자.

```Kotlin
@Test
@DisplayName("앨범 정보를 수정한다.")
fun updateAlbum_TEST() {
    // "'Round Midngiht"에서 '를 제거한 타이틀로 변경.
    val request = AlbumRequest(id = 7, title = "Round Midnight")
    val updateAlbum = albumService.updateAlbum(request)
    with(updateAlbum) {
        assertThat(musicianName).isEqualTo("Thelonious Monk")
        assertThat(title).isEqualTo("Round Midnight")
    }

}
```

이제는 컨트롤러도 만들어 보자.

```Kotlin
/**
 * 뮤지션 정보를 변경하고 변경된 정보를 담아서 보내준다.
 * @param request: AlbumRequest
 * @return ResponseResult<AlbumDto>
 */
@PatchMapping("/albums")
fun updateAlum(@RequestBody request: AlbumRequest) = ResponseResult.of(albumService.updateAlbum(request))
```

호출을 해보면 변경이후 정보를 잘 보여 줄 것이다.

```json
// -PATCH localhost:8081/api/music/store/albums
// '를 다시 붙여서 업데이트 하자.
{
  "id": 7,
  "title" : "'Round Midnight"

}

// response
{
  "result": {
    "id": 7,
    "musicianName": "Thelonious Monk",
    "title": "'Round Midnight",
    "albumType": "정규음반",
    "representative": "Y"
  }
}

```

# 이전 브랜치에서 추가한 ObjectMapper관련 추가 내용

```Kotlin
/**
 * json 스트링이 리스트 형식인 경우 해당 객체로 매핑해서 리스트 형식으로 반환한다.
 */
fun <T> convertToObject(json: String, valueType: TypeReference<T>): T = mapper.readValue(json, valueType)

/**
 * TypeReference를 편하게 쓰기 위한 메소드
 */
fun <T> typeRef(valueType: Class<T>): TypeReference<List<T>> = object: TypeReference<List<T>>() {}


// 테스트 코드
@Test
@DisplayName("objectMapper type")
fun objectMapperList_TEST() {
    val list = listOf(Musician(id = 1, name =  "test111", genre = GenreCode.JAZZ), Musician(id = 2, name =  "test222", genre = GenreCode.JAZZ))
    val json = toJson(list)
    println(json)
    val convertToObj = convertToObject(json, typeRef(Musician::class.java))
    println(convertToObj)
}
```
이런 코드를 만들었었다.

하지만 typeRef메소드를 살펴보면 실제 valueType 이 부분은 사용되지 않는다.

그저 타입 유지를 위해서 변수로 넘길 뿐이다. 물론 코드는 잘 돌아간다.

그렇지만 이런 방식은 어떨까????

```Kotlin
    typeRef<Musician>()
```

이것을 위해서 코틀린에서는 inline함수와 reified키워드를 제공하고 있다.

[kotlin inline functions](https://codechacha.com/ko/kotlin-inline-functions/)

따라서 이것을 활용해서 코드를 변경해 보자.

```Kotlin
/**
 * json 스트링이 리스트 형식인 경우 해당 객체로 매핑해서 리스트 형식으로 반환한다.
 */
fun <T> convertToObject(json: String, valueType: TypeReference<T>): T = mapper.readValue(json, valueType)

/**
 * TypeReference를 편하게 쓰기 위한 메소드
 */
inline fun <reified T> typeRef(): TypeReference<List<T>> = object: TypeReference<List<T>>() {}


// test code

@Test
@DisplayName("objectMapper type")
fun objectMapperList_TEST() {
    val list = listOf(Musician(id = 1, name =  "test111", genre = GenreCode.JAZZ), Musician(id = 2, name =  "test222", genre = GenreCode.JAZZ))
    val json = toJson(list)
    println(json)
    val convertToObj = convertToObject(json, typeRef<Musician>())
    println(convertToObj)
}
```

# log를 남기고 싶은데요?

자 위에 inline 함수와 reified키워드를 뜬금없이 소개했다.

이유는 이제부터 설명해 보고자 한다.

자바를 쓸 때 우리는 롬복을 통해 단순하게 어노테이션만 붙이고 로그를 사용하는 편리함을 잊을 수 없다.

근데 여기서는 롬복을 쓰지 않는다.

물론 코틀린에서는 롬복과 관련된 플러그인을 제공하긴 하지만 몇가지 제공을 하지 않는다.

그런데 하필 그 중에 하나가 @Slf4j이다.

애초에 쓸 생각이 없으므로 다른 방법을 찾아야 한다.

## 첫 번재 방법

위에서 언급했던 inline함수와 reified키워드를 이용한다.

[Idiomatic Logging in Kotlin](https://www.baeldung.com/kotlin/logging)

일단 찾아본 것은 baeldung에서 사용하는 방식이다.

이 사이트에서 소개하는 것은 각 클래스마다

```Kotlin
private val logger = LoggerFactory.getLogger(javaClass)
```

처럼 작성해서 사용하는 방법을 최종적으로 다음과 같이

```Kotlin
/**
 * 클래스별로 로거 설정할 수 있도록 Inline, reified를 통해 제너릭하게 사용해 보자.
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)
```

로 선언하고

```Kotlin

val log = logger<AlbumController>()

/**
 * 앨범 리스트를 반환한다.
 * @return ResponseResult<List<AlbumDto>>
 */
@GetMapping("/albums")
fun albums(pagination: Pagination): ResponseResult<List<AlbumDto>> {
    log.info("로깅을 해보자 ")
    val albumsWithPage = albumService.fetchAlbums(PageRequest.of(pagination.offset, pagination.limit))
    setPagination(pagination, albumsWithPage)
    return ResponseResult.of(albumsWithPage.content, pagination)
}

```
처럼 좀 더 코드를 간결하게 사용하는 방식을 소개하고 있다.

## 라이브러리 사용

[kotlin-logging](https://github.com/MicroUtils/kotlin-logging)

이런 라이브러리가 있다고 한다.

하지만 사용법을 보면

```Kotlin
private val logger = KotlinLogging.logger {}
```
같은 방식을 제공하고 몇가지 어드밴티지가 있다.

실제로 깃헙의 Overview에 대한 내용을 보면 이런 이야기를 언급하는데

```
A straight forward way to log messages with lazy-evaluated string using lambda expression {}.
```
이 부분이 아마 이 라이브러리를 사용하는게 가장 큰 매력이 아닐까?

하지만 여기서는 저 라이브러리를 사용하지 않을 생각이다.

만일 해당 라이브러리를 사용해야 할 상황이 발생하면 써볼 예정이다.

이제는 로그를 남길 수 있으니 logback설정을 해 보자.


```yml
logging:
  config: classpath:logback-spring.xml
```

사실 이건 스프링 부트의 설정과 별반 다를게 없고 logback-spring.xml역시 별반 다를게 없다.

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="local">
        <include resource="./logback-local.xml" />
    </springProfile>
    <springProfile name="dev">
        <include resource="./logback-dev.xml" />
    </springProfile>
</configuration>


<!-- profile별로 작성하는 logback-local.xml -->
<included>

<include resource="org/springframework/boot/logging/logback/defaults.xml"/>
<conversionRule conversionWord="clr" converterClass="org.springframework.boot.logging.logback.ColorConverter"/>
<conversionRule conversionWord="wex" converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter"/>

<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>[%d{YYYY-MM-dd HH:mm:ss z, Asia/Seoul}] [%-5level] [%thread] %logger{36}:%L - %msg -- %X{log.remoteAddr}%n</pattern>
    </encoder>
</appender>

<property name="LOG_DIR" value="./logs"/>
<property name="FILE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%t] %C:%L - %msg%n%wex"/>

<appender name="dailyRollingFileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_DIR}/musicstore-local.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOG_DIR}/musicstore-local.%d{yyyy-MM-dd}.log.%i.gz</fileNamePattern>
        <maxHistory>30</maxHistory>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>100MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
    </rollingPolicy>
    <encoder>
        <pattern>${FILE_LOG_PATTERN}</pattern>
    </encoder>
</appender>

<logger name="io.basquiat.musicstore" additivity="false" level="DEBUG">
    <appender-ref ref="dailyRollingFileAppender"/>
    <appender-ref ref="CONSOLE" />
</logger>

<logger name="org.springframework" additivity="false" level="INFO">
    <appender-ref ref="dailyRollingFileAppender"/>
    <appender-ref ref="CONSOLE" />
</logger>

<root level="INFO">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="dailyRollingFileAppender"/>
</root>

</included>



```
애초에 스트링 부트에 포함되어 있는 logging을 쓰고 xml을 설정하기 때문에 이 부분은 코틀린과는 무관하니 설정 그대로 가져다 쓰면 된다.

또한 root의 레빌이 info인 경우 일반적으로 console로만 찍고 파일을 쓰지 않지만 원하는 대로 작동하는지 확인하기 위해 ref를 설정했다.

지금까지 인식못했다가 common패키지의 위치가 이상해서 musicstore안으로 옮겼다.      

# At a Glance

뮤지션 부분도 음반과 마찬가지로 create/update부분을 변경했다.

변경 또는 생성된 정보를 반환해 줄 것인지 아니면 성공/실패 정보만 보내줄 것인지는 상황에 맞춰서 작성하면 된다.

언제나 이 부분은 나의 방식이 답이 아닐 수 있기 때문에 상황에 맞게 유연하게 대처하는게 중요하다.

그리고 좀 더 어플리케이션스럽게 로깅 설정도 해봤다.         

다음 브랜치에서 몇가지 기능들을 추가해 볼 생각이다.        