# 코틀린을 향해 움직여라!

오래전 코틀린이 처음 등장했을 때 나는 굳이 이걸 배워야하나?

라는 건방진 생각을 한 적이 있다.

하지만 임백준 형님의 폴리그랏 관련 연사를 듣고 책을 읽으면서 무시했던 nodeJs와 코틀린을 조금씩 배웠고 관련 언어로 실제 프로덕트에서 활용한 경험은 많은 생각을 하게 했다.

이게 7년전 이야기니 코틀린이 이렇게 각광받으리라고는 생각을 했다!!!

어쨰든 몇 년동안은 코틀린을 이용할 일이 없어서 감각이 엄청 둔해지니 뭔가 퇴화된다는 생각과 나이를 먹어감에 따라 경각심이 심히 커져 이 프로젝트를 만들게 되었다.

또한 회사에도 코틀린을 한번 도입을 해보고 싶은 욕심도 있고하니 시작해 보자!

기본적으로 코틀린을 어느 정도 알고 있다는 가정하에 시작하는 자바를 아시더라도 코틀린에 대한 정보가 전혀없다면 관련 공부를 조금씩 해두는게 좋다.

# Spring boot with JPA

이제는 가장 먼저 생각나는 조합이 아닌가?

이제부터 하나하나 만들어 볼까 한다.

Start!!!!!

# 요구 사항

Spring boot의 최신 버전을 사용하기 때문에 gradle version 7.4.2를 사용하고 있다.        
java version은 11이다.

# Simple Entity

일단 나는 music-store를 하나 만들어볼까 한다.

예전에는 엔티티와 관련해서 no arg관련 문제로 allOpen같은 plugin을 사용하고 관련 세팅을 따로 해줬던 걸로 기억하는데 지금은 버전이 되면서 jpa관련 플러그인이 나왔다.

관련 세팅 정보는 build.gradle를 참조하면 될 듯 싶다.

우선 뮤지션 정보를 담는 엔티티가 필요할텐데 자바 스타일이라면 다음과 같을 것이다.

```Kotlin

@Entity
@Table(name = "musician")
public class Musician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String genre;

}

```

뮤지션은 앨범 정보를 가질 수 있지만 심플하게 뮤지션의 정보만을 담는 초간단 심플 엔티티이다.

그렇다면 이것을 코틀린으로 바꾸겠다면 어떻게 할 것인가?

```Kotlin
@Entity
@Table(name = "musician")
class Musician(

    var name: String,

    var genre: String? = "etc",

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    ) {

    init {
        if(name.isBlank()) {
            throw MandatoryArgumentException("뮤지션의 이름이 누락되었습니다. 뮤지션의 이름을 확인하세요.")
        }
    }

}

```

Long이나 String뒤에 ?는 null-safe와 관련된 코드로 해당 변수가 null이라면 그 이후 선언된 값으로 표시하라는 의미이다.

MandatoryArgumentException를 하나 만들어보자. IlleganArgumentException을 그대로 사용할 수 있지만 코틀린의 특징 중 하나를 한번 사용해보고자 한다.

MandatoryArgumentException.java

```java
/**
 * MandatoryArgumentException 관련 에러 처리 exception
 * 필수 정보가 없다면 이 익셉션으로 처리하자.
 * created by basquiat
 */
public class MandatoryArgumentException extends RuntimeException {

    /**
     * Constructor with one parameter
     * @param message
     */
    public MandatoryArgumentException(String message) {
        super(message);
    }

}

```

일반적인 자바라면 위 코드와 같이 생성할 수 있다.

하지만 코틀린의 경우 상속 또는 구현, 그러니깐 extends/implements든간에 다음과 같이 ':'으로 구분하고 뒤에 기입을 한다.

이때 특징은 클래스라면 부모 클래스의 생성자를 호출한다.

여럿일 경우에는 방법이 다양한데 일단 위 경우를 살펴본다면 부모 클래스의 생성자는 'super(message) 죽 RuntimeException(message)'이기 때문에 코틀린으로 처리한다면 아래와 같다.

인터페이스의 경우에는 인터페이스를 표기하면 된다.



MandatoryArgumentException.kt

```Kotlin
/**
 * NotFoundException 관련 에러 처리 exception
 * 필수 정보가 없다면 이 익셉션으로 처리하자.
 * created by basquiat
 */
class MandatoryArgumentException(message: String? = "필수 정보가 누락되었습니다.") : RuntimeException(message)

```

String보다는 enum으로 정의해서 사용하는 방식이 가능하기 때문에 GenreCode enum을 작성한다.

```Kotlin
/**
 * 장르 코드 정의 enum
 */
enum class GenreCode(val genre: String) {

    JAZZ("Jazz"),
    ROCK("Rock"),
    POP("Pop"),
    HIPHOP("Hiphop"),
    WORLD("World Music"),
    ETC("etc");

    companion object {
        /**
         * null이면 illegalArgumentException을 던지고 있지만 ETC를 던져도 상관없다.
         * @param genre
         * @return GenreCode
         */
        fun of(genre: String?): GenreCode {
            return values().firstOrNull { genreEnum -> genreEnum.genre.equals(genre, ignoreCase = true) }
                ?: throw IllegalArgumentException("맞는 장르 코드가 없습니다. 장르 코드를 확인하세요.")
        }
        /*
        fun of(genre: String?): GenreCode {
            return values().firstOrNull { genreEnum -> genreEnum.genre.equals(genre, ignoreCase = true) }
                   ?: ETC
        }*/

    }

}
```
companion object는 자바에서 말하는 정적 메소드 생성하는 부분과 같다.

코틀린에서 자바의 Stream API를 사용할 수 있지만 lodash와 비슷한 자체 Collection를 제공하기 때문에 코드를 좀더 간결하게 작성하기 위해 적극 사용한다.

스트림이나 콜렉션에서 제공하는 API들의 형태는 자바와 비슷하지만 블록 코드가 약간 다른 면이 있다. 이것은 관련 정보를 찾아보면 잘 나온다.

## 빌드 패턴을 적용하고 싶은데요?

코틀린과 관련해서 클래스와 인터페이스에 대한 내용을 여기서 일일히 언급하기에는 힘이 부친다.

게다가 나는 롬복을 사용하고 싶지 않다. 왜냐하면 코틀린에서 대부분 이것을 지원해 주기 때문이다.

~~어느 정도의 편의성을 좀 버려야 한다~~

빌드 패턴을 적용하는 다양한 방법이 있는데 여기서는 baeldong에서 소개하는 방식을 활용한 빌드 패턴을 차용하고자 한다.


일반적으로 constructor는 주 생성자를 의미해서 앞에 특정 키워드가 없다면 생략가능하지만 빌드 패턴을 활용하기 위해서 private로 만든다.

즉 일반적인 방식으로는 생성자를 통한 dto 생성이 불가능해진다.

물론

```
Musician("name", genre, id)
```
처럼도 가능하게 하고 싶다면 private constructor를 선언하지 않아도 무방하다.


```Kotlin
@Entity
@Table(name = "musician")
class Musician private constructor(

    var name: String?,

    @Enumerated(EnumType.STRING)
    var genre: GenreCode?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    ) {

    init {
        if(name == null) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = Musician(name, genre, id)
    }

}

```

```Kotlin

class SimpleTest {

    @Test
    @DisplayName("빌드 패턴 적용")
    fun builderPattern_TEST() {
        var m = Musician.Builder()
            .name("Charlie Parker")
            .genre(GenreCode.JAZZ)
            .build()
        println(m.name)
    }
}


```

다만 단점이라면 dto라든가 엔티티에서 이 빌드패턴을 사용할 때 변수가 많으면 진짜.......

## Setter와 Getter에 대한 고찰

객체를 생성할 때 val과 var의 차이를 알아야 한다.

그리고 자바처럼

```
obj.setName("name") 또는 obj.getName()
```

같은 방식이 아니고 자바스크립트나 고랭처럼

```
obj.name = "change name" 또는 val name = obj.name
```
같은 방식이다.

일반적으로 var로 하게 되면 setter와 getter를 전부 열어두는 방식이고 val의 경우에는 getter의 경우에만 public하고 setter에 대해서는 닫혀있다.

JPA와 관련해서 엔티티를 생성할 때 개발자의 습꽌이나 코딩 스타일에 따라 이것은 몇가지 불편함이 존재하는데 다음과 같은 방식으로 코드 컴벤션을 만들어가는게 최선이다.

### 일단 오픈!!!!!

일단 다 열어두고 팀의 개발 문화에 엔티티의 경우에는 특별한 경우가 아니라면 setter를 사용하지 않는다는 문화를 만드는 경우이다.

사실 코틀린을 사용하는 이유는 코드의 간결함이 우선인 경우가 많다.

그리고 코틀린에 국한하지 않고 자바에서도 마찬가지로 통용될 수 있는 방법이다.

따라서 자바 스타일이라면 어떤 변수의 값을 변경한다면 통상적인 방식으로 'changeName'같은 함수를 만들어 이름을 주고 이름을 변경한다는 명확한 의미를 가진 메소드를 통해 변경하도록 한다.

### 커스텀 Setter

```Kotlin
@Entity
@Table(name = "musician")
class Musician(
    name: String?,

    @Enumerated(EnumType.STRING)
    var genre: GenreCode?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    ) {

    init {
        if(name == null) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

    var name: String? = name
        private set

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = Musician(name, genre, id)
    }

}
```
생성자에서는 다음과 같이 타입을 정하지 않고 객체의 바디쪽에 var로 선안한다.

이떼 private set을 통해서 setter를 아예 막는 방식이다.

private 대신에 protected를 사용하는 방식도 있다.

```Kotlin
@Entity
@Table(name = "musician")
class Musician(

    name: String?,

    @Enumerated(EnumType.STRING)
    var genre: GenreCode?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    ) {

    init {
        if(name == null) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

    var name: String? = name
        protected set

    fun changeName(name: String) {
        this.name = name;
    }

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = Musician(name, genre, id)
    }

}
```

이렇게 하면

```
musician.name = "changeName"
```
처럼 값을 변경할 수 없고 changeName 메소드를 통해서 이름을 변경할 수 있다.

### Backing Property

backing field방식과는 다르게 변수명에 대한 스키마와 다르게 작업을 한다면 고려해 볼만한 방식이다.


```Kotlin

@Entity
@Table(name = "musician")
class Musician(

    private var _name: String?,

    @Enumerated(EnumType.STRING)
    var genre: GenreCode?,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    ) {

    init {
        if(_name == null) {
            throw MandatoryArgumentException("뮤지션 이름은 필수 입니다.")
        }
    }

    val name get() = this._name

    fun changeName(name: String) {
        this._name = name
    }

    data class Builder(
        var name: String? = null,
        var genre: GenreCode? = GenreCode.ETC,
        var id: Long? = null
    ) {
        fun name(name: String) = apply { this.name = name }
        fun genre(genre: GenreCode) = apply { this.genre = genre }
        fun id(id: Long) = apply { this.id = id }
        fun build() = Musician(name, genre, id)
    }

}
```

생성자에서 private var로 선언하고 _name처럼 변수명을 선언한다. 이게 관례라고 하는데 그렇다고 하니까 그렇게 사용하자.

private var로 선언하게 되면 내부에서만 접근이 가능하게 된다.

이후에 객체의 바디에 val로 선언을 해서 이 값을 반환하게 하는 방식이다. setter는 필요하다면 changeName같은 메소드를 사용하면 된다.

어떤 방식이든 상황에 맞게 사용하면 좋겠지만 여기서는 코틀린의 간결함을 추구하고자 하기 때문에 오픈하는 방식으로 갈 예정이다.

관련 내용은 테스트로 남긴다.

# 일단 질러보자

컨트롤러를 하나 만든다.


```Kotlin
/**
 * musician의 정보와 관련된 컨트롤러
 * created by basquiat
 */
@RestController
@RequestMapping("/api/music/store")
class MusicianController {

    @GetMapping("/musicians")
    fun musicians() : List<Musician> {
        return listOf(Musician.Builder()
            .name("Chalie Parker")
            .genre(GenreCode.JAZZ)
            .build(),
            Musician.Builder()
                .name("Miles Davis")
                .genre(GenreCode.JAZZ)
                .build()
        )
    }

}
```

로직은 레이어를 나누지 않고 단순하게 컨트롤러단에서 몇개의 정보를 보내주는 방식으로 진행한다.

포스트맨이나 몇 몇 RESTful 도구를 사용해서 날려보자.

현재 만든 녀석은 GET이니 브라우져에서 바로 다음 주소로 때려봐도 된다.

http://localhost:8081/api/music/store/musicians

```json
[
  {
    "name": "Chalie Parker",
    "genre": "JAZZ",
    "id": null
  },
  {
    "name": "Miles Davis",
    "genre": "JAZZ",
    "id": null
  }
]
```
여기까지 왔다면 여러분은 가장 기본적인 Spring boot 세팅과 코틀린에서 객체를 다루는 방식을 어느 정도 공부하게 된 것이다. 👏

# Service Layer를 만들어보자

이제는 정말 초간단하게 컨트롤로에 있는 저 녀석을 Service 레이어로 옮겨보자.

```Kotlin
/**
 * 뮤지션의 정보를 다루는 서비스 객체
 * created by basquiat
 */
@Service
class MusicianService {

    /**
     * 뮤지션의 정보를 가져온다.
     * @return List<Musician> 뮤지션 리스트
     */
    fun fetchMusicians(): List<Musician> {
        return listOf(Musician.Builder()
            .name("Chalie Parker")
            .genre(GenreCode.JAZZ)
            .build(),
            Musician.Builder()
                .name("Miles Davis")
                .genre(GenreCode.JAZZ)
                .build()
        )
    }

}
```
캬! 초간단하다. 그냥 컨트롤러에 있는 코드를 단순하게 옮겼다.

그렇다면 이제는 이녀석을 컨트롤러에 DI를 받도록 기존에 만들어 놓은 MusicianController를 수정하자.


```Kotlin
/**
 * musician의 정보와 관련된 컨트롤러
 * created by basquiat
 */
@RestController
@RequestMapping("/api/music/store")
class MusicianController(
    private val musicianService: MusicianService,
) {

    @GetMapping("/musicians")
    fun musicians() : List<Musician> {
        return musicianService.fetchMusicians()
    }

}
```
오 이제 다시 한번 테스트를 해보자.

```Kotlin
@SpringBootTest
class MusicianServiceTest @Autowired constructor(
    private val musicianService: MusicianService,
) {

    @Test
    @DisplayName("뮤지션 정보를 가져오는 메소도를 테스트한다.")
    fun fetchMusicians_TEST() {
        val musicians = musicianService.fetchMusicians()
        assertThat(musicians).hasSize(2)
    }
}
```

이제는 서버를 다시 실행해서 포스트맨이나 브라우저에서 확인해보자.

# 그렇다면 이제 Persistence 레이어를 작성하자.

이제는 드디어 RDBMS와 JPA를 활용해보자.

일단 이 프로젝트는 내 컴퓨타에 mySql이 설치되어 있으니 mySql를 활용한다.

H2같은 경우에는 다음과 같이 build.gradle에 mySql대신에

```
    runtimeOnly 'com.h2database:h2'
```
를 선언하고

appliction.yml에 다음과 같이

```
spring:
  datasource:
    url: 'jdbc:h2:mem:basquiat'
    username: '<<your name>>'
    password: '<<your password>>'
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        show_sql: true
  h2:
    console:
      enabled: true
      # 패스는 편한대로
      path: '/h2-console'

```
지금은 준비된 스키마와 인서트 쿼리를 나의 경우에는 직접 넣어서 테스트를 실행할 예정이다.

H2의 경우에도 콘솔창으로 접근해서 해도 상관없다.

이것도 싫다면 서버 실행시 데이터를 밀어넣게 만드는 방법을 고려하면 된다.


```sql

CREATE TABLE `musician` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `genre` enum('JAZZ','ROCK','POP','HIPHOP','WORLD','ETC') NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='뮤지션 초간단 정보를 담는 테이블';

INSERT INTO 
	musician (name, genre)
VALUES 
	('Charlie Parker', 'JAZZ'),
	('John Coltrane', 'JAZZ');
```

나의 경우에는 일종의 하나의 템플릿 같은 언터페이스를 사용한다.

```kotlin
/**
 * jpaRepository Base -> 도메인별 레파지토리 생성시 해당 인터페이스를 상속해서 사용한다.
 * @param M entity
 * @param ID entity ID
 */
@NoRepositoryBean
interface BaseRepository<M, ID : Serializable?> : JpaRepository<M, ID>, JpaSpecificationExecutor<M>
```

이제는 MusicianRepository를 작성하자.

```Kotlin
/**
 * MusicianRepository
 */
interface MusicianRepository : BaseRepository<Musician, Long>
```
SooooOOO~~~~ SIMPLE!!!!!

이제는 MusicianService에 이녀석을 DI받자

```Kotlin
/**
 * 뮤지션의 정보를 다루는 서비스 객체
 * created by basquiat
 */
@Service
class MusicianService(
    private val musicianRepository: MusicianRepository
) {

    /**
     * 뮤지션의 정보를 가져온다.
     * @return List<Musician> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(): List<Musician> {
        return musicianRepository.findAll()
    }

}
```
이제는 아까 만들어 놓은 테스트 코드를 실행해 보자.

하지만 우리는 리스트 자체를 보내기보다는 응답객체에 담아서 보내보려고 한다.


```Kotlin
/**
 * Rest API response 정보를 담은 객체
 */
data class ResponseResult<T>(
    private var _result: T?,
) {

    val result get() = this._result

    companion object {
        /**
         * ResponseResult를 생성하는 정적 메소드
         * @param result
         * @param <T>
         * @return ResponseResult<T>
         */
        fun <T> of(result: T): ResponseResult<T> {
            return ResponseResult(result)
        }
    }

}
```
사실 Backing Property를 활용하지 않아도 되지만 간지나보이는거 같아서 이렇게 작성해 보자.

정보를 담아 내주는 객체로 data class로 만든다. 그리고 companion object를 이용해 정적메소드를 제공하자.

최총 MusicianController는 다음과 같이 바뀔 것이다.

```Kotlin
/**
 * musician의 정보와 관련된 컨트롤러
 * created by basquiat
 */
@RestController
@RequestMapping("/api/music/store")
class MusicianController(
    private val musicianService: MusicianService,
) {

    @GetMapping("/musicians")
    fun musicians() : ResponseResult<List<Musician>> {
        return ResponseResult.of(musicianService.fetchMusicians())
    }

}
```

이걸로도 뭔가 부족하다. entity를 dto에 담아서 보내고 싶은 욕구가 마구 생긴다.

```Kotlin
/**
 * musicianDTO
 * created by basquiat
 */
data class MusicianDto(
    val id: Long,
    val name: String,
    private var _genreCode: GenreCode,
) {
    /**
     * 클라이언트로 정보를 내려줄 때는 code값으로 보내주기 위해 backing property를 사용
     */
    val genre: String get() = this._genreCode.genre

    companion object {
        /**
         * @param musician
         * @return MusicianDto
         */
        fun create(musician: Musician): MusicianDto = with(musician) {
            MusicianDto(name =  name!!, id = id!!, _genreCode = genre!!)
        }
        /*
        fun create(musician: Musician): MusicianDto {
            return MusicianDto(musician.id!!, musician.name!!, musician.genre!!)
        }
        */
    }

}
```
심플한 dto를 만들고 companion object를 이용해 엔티티를 받는 메소드를 통해 dto를 반환하게 만들자.

빌드 패턴을 활용해도 무방하다!!!

하지만 위 코드를 보면 신기한 것을 알 수 있는데 일반적으로 생성자를 통한 객체 생성은 순서가 맞아야 한다.

하지만 와와 같이 해당 객체의 생성자에 있는 변수명으로 접근하면 순서와는 상관없이 작동하게 된다.

빌드 패턴이 코드의 간결함에 방해가 된다면 이 방식을 통해서 처리하는 방식도 좋다.

앞으로의 코드는 이 방식을 통해서 작성할 예정이다.

그리고 with를 사용해서 코드를 더 간략하게 처리하자!

Scope Function에는 with, run, let, also, takeIf, takeUnless와 빌드 패턴시 사용한 apply가 있는데 이 부분 역시 인터넷에 잘 나와있다.

backng property를 이용한 이유는 enum코드를 받아 code정보를 화면에 보내주기 위해서이다.

```Kotlin
/**
 * 뮤지션의 정보를 다루는 서비스 객체
 * created by basquiat
 */
@Service
class MusicianService(
    private val musicianRepository: MusicianRepository
) {

    /**
     * 뮤지션의 정보를 가져온다.
     * @return List<Musician> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(): List<MusicianDto> {
        return musicianRepository.findAll()
            .map {entity -> MusicianDto.create(entity)}
        //.map(MusicianDto.Companion::create)

    }

}
```
toList()는 생략해도 상관없다.

map을 보면 코드 블락이 좀 다른 것을 알 수 있다.

입맛에 맞는 방식을 사용하는 것은 개발자 몫!

기존의 만들어 놓은 테스트 서비스 코드는 변화가 없을 것이다.

서버를 실행해서 API를 날려보면

```json// 20220817154453
// http://localhost:8081/api/music/store/musicians

{
  "result": [
    {
      "id": 1,
      "name": "Charlie Parker",
      "genre": "Jazz"
    },
    {
      "id": 2,
      "name": "John Coltrane",
      "genre": "Jazz"
    }
  ]
}
```

genre에서는 대문자 코드가 아닌 내부에 선언한 코드 정보가 제대로 나오는 것을 확인할 수 있다.

전체적으로 코드가 자바에 비해 엄청 간결해진것을 알게 된다.

여러분은 이제 Spring boot를 활용한 가장 기본적인 웹의 컨셉을 적용해 자바에서 코틀린으로 변경했다. 👊

# At A Glance

다음 브랜치에서는 나머지 기능을 구현해 볼 생각이다. 👋