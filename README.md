# 시작하기 전에
이전 브랜치에서 예제로 다뤘던 테스트와 엔티티는 삭제하고 빌드 패턴 사용을 하지 않을 예정이기에 불필요한 코드는 삭제.

다만 빌드 패턴의 경우에는 변수가 많아질 경우 사용할 예정이다.

# 조회를 해보자.

이전 브랜치에서는 뮤지션의 정보를 가져오는 초간단 API를 만들었다. 게다가 페이징 처리도 하지 않은 API이다.

일단 페이징 처리는 나중에 하고 지금 가장 당장 필요한 것은 조회를 통한 뮤지션 정보를 가져오는 것이다.

여기서 우리는 3가지 API를 만들 수 있다는 것을 알 수 있다.

아이디/뮤지션 이름/장르를 통한 조회이다.      

아이디를 통한 조회는 하나의 정보만을 반환한다는 것을 알 수 있다.

장르를 통한 뮤지션 정보 역시 리스트로 반환될 수 있다.

이름의 경우에는 고민을 해봐야 하는데 이유는 동명 이인을 생각해 볼 수 있기 때문이다.

동명 이인이 존재할 수 있지만 단일 정보로 반환하자.

기존의 MusicianRepository은 다음과 같다.

```Kotlin
interface MusicianRepository : BaseRepository<Musician, Long>
```

이름과 장르를 통한 조회용 메소드가 필요하니 다음과 같이 변경해 보자.


```Kotlin
/**
 * MusicianRepository
 */
interface MusicianRepository : BaseRepository<Musician, Long> {

    fun findByName(name: String) : Optional<Musician>
    fun findByGenre(genre: GenreCode) : List<Musician>

}
```

실제로 서비스에서 이것을 가지고 코드를 작성하자.


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
                                 .map(MusicianDto::create)

    }

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return Optional<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long): Optional<Musician> {
        return musicianRepository.findById(id)
    }

    /**
     * 뮤지션 이름으로 뮤지션 리스트를 가져온다.
     * @return Optional<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByName(name: String): Optional<Musician> {
        return musicianRepository.findByName(name)
    }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode): List<Musician> {
        return musicianRepository.findByGenre(genre)
    }

}
```

테스트 코드도 한번 만들어보자.

ObjectEmptyException은 의도적으로 만들어 둔 Exception이다.

```Kotlin
@SpringBootTest
class MusicianServiceTest @Autowired constructor(
    private val musicianService: MusicianService,
) {

    @Test
    @DisplayName("뮤지션 정보를 가져오는 메소드를 테스트한다.")
    fun fetchMusicians_TEST() {
        val musicians = musicianService.fetchMusicians()
        assertThat(musicians).hasSize(2)
    }

    @Test
    @DisplayName("아이디로 뮤지션 정보를 가져오는 메소드를 테스트한다.")
    fun fetchMusicianById_TEST() {
        val musicianId = 1L
        val musician = musicianService.fetchMusicianById(musicianId).orElseThrow(::ObjectEmptyException)
        assertThat(musician.name).isEqualTo("Charlie Parker")
    }

    @Test
    @DisplayName("이름으로 뮤지션 리스트를 가져오는 메소드를 테스트한다.")
    fun fetchMusiciansByName_TEST() {
        val musicianName = "Charlie Parker"
        val musician = musicianService.fetchMusicianByName(musicianName)
        assertThat(musician.get()?.name).isEqualTo("Charlie Parker")
    }   

    @Test
    @DisplayName("장르로 해당 뮤지션 리스트를 가져오는 메소드를 테스트한다.")
    fun fetchMusiciansByGenre_TEST() {
        val genre = GenreCode.JAZZ
        val musicians = musicianService.fetchMusiciansByGenre(genre)
        assertThat(musicians).hasSize(2)
        assertThat(musicians[0].name).isEqualTo("Charlie Parker")
        assertThat(musicians[1].name).isEqualTo("Charlie Parker") // 의도적 에러
    }

}
```

여기서 우리는 코틀린을 사용한다.

하지만 다음 글을 한번 살펴보자.

[Kotlin Nullable Types vs. Java Optional](https://medium.com/@fatihcoskun/kotlin-nullable-types-vs-java-optional-988c50853692)

번역글도 있다.

[Kotlin Nullable Types vs. Java Optional 번역본](https://medium.com/@limgyumin/%EC%BD%94%ED%8B%80%EB%A6%B0-nullable-%ED%83%80%EC%9E%85-vs-%EC%9E%90%EB%B0%94-optional-e698adc6d617)


실제로 지금 회사의 Optional과 관련된 코드를 살펴보면 참 재미있는게 sonarlint를 살펴보면 isEmpty() 나 isPresent()사용을 권고한다.

하지만 if문의 증가와 개발자의 귀차니즘이 점철되어 그냥 get()하고 말아버린다.  ~~널 보낼수 있다면?~~

게다가 엘비스 오퍼레이터를 지원하지 않는 자바라 이게 여간 귀찮은게 아니다.

또한 가만히 보면 코틀린 역시 자바스크립트와 상당히 비슷한 부분이 있어 보이는 데 코틀린은 확장함수를 지원한다.

확장함수는 마치 자바스크립트에서 prototype이랑 상당히 비슷하다.

예를 들면


```javascript
function Basquiat(name) {
    this.name = name;
}

Basquiat.prototype.convertToEngName = () => {
    return name + "eng";
}

let basquiat = new Basquiat("test");

basquiat.convertToEngName();
// testeng

```
이런 식으로 기존의 객체에 메소드와 프로퍼티를 추가할 수 있다.

MusicianRepository를 다시 한번 확인해 보자.

```Kotlin
/**
 * MusicianRepository
 */
interface MusicianRepository : BaseRepository<Musician, Long> {

    fun findByName(name: String) : Optional<Musician>
    fun findByGenre(genre: GenreCode) : List<Musician>

}
```

상속한 인터페이스 BaseRepository는 JpaRepository<M, ID>, JpaSpecificationExecutor<M>를 또 상속하고 있다.

여기서 따라 들어가보면 CrudRepository를 만나게 되는데

```java
/**
 * Interface for generic CRUD operations on a repository for a specific type.
 *
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Jens Schauder
 */
@NoRepositoryBean
public interface CrudRepository<T, ID> extends Repository<T, ID> {
 
	/**
	 * Retrieves an entity by its id.
	 *
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	Optional<T> findById(ID id);
 
}

```
findById는 바로 Optional을 반환한다.

코틀린에서는 이것을 확장 함수를 이용해 CrudRepositoryExtensions.kt를 제공한다.

```Kotlin
/**
 * Retrieves an entity by its id.
 *
 * @param id the entity id.
 * @return the entity with the given id or `null` if none found
 * @author Sebastien Deleuze
 * @since 2.1.4
 */
fun <T, ID> CrudRepository<T, ID>.findByIdOrNull(id: ID): T? = findById(id).orElse(null)

```

자바스립트의 prototype과 상당히 유사한 방식이다.

그렇다면 Optional을 제거하고 nullable하게 사용하기 위해 기존의 코드를 수정해 보자.

```Kotlin
/**
 * MusicianRepository
 */
interface MusicianRepository : BaseRepository<Musician, Long> {

    fun findByName(name: String) : Musician?
    fun findByGenre(genre: GenreCode) : List<Musician>

}
```
Optional대신에 ?를 붙여주자.

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
                                 .map(MusicianDto::create)

    }

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return Optional<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long): Musician {
        return musicianRepository.findByIdOrNull(id) ?: throw ObjectEmptyException("id로 조회된 뮤지션 정보가 없습니다.")
    }

    /**
     * 뮤지션 이름으로 뮤지션 리스트를 가져온다.
     * @return Musician?
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByName(name: String): Musician? {
        return musicianRepository.findByName(name)
    }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode): List<Musician> {
        return musicianRepository.findByGenre(genre)
    }

}
```

CrudRepositoryExtensions.kt를 가만히 살펴보면 이 녀석을 흉내내서 무언가 해볼 수 있지 않을까???

예를 들면 우리는 null을 반환하기 보다는 아이디로 조회된 정보가 없다면 이것을 에러라고 판단하고 에러를 던진다면 상황에 따라 선택적으로 사용할 수 있다는 것을 알 수 있다.

다만 이름 조회는 null일 수 있기 때문에 null을 반환하는 방식이 좋아보인다. 그래야 유저한테 찾는 정보가 없다는 것을 알려 줄 수 있을테니까.       

common패키지에 extensions라는 패키지를 만들고 저걸 흉내내보자.

CustomCrudRepositoryExtensions.kt
```Kotlin
/**
 * null을 반환하지 않고 ObjectEmptyException에러를 날리자.
 */
fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T? = this.findByIdOrNull(id) ?: throw ObjectEmptyException()

```

그리고 이것을

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
     * @return List<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(): List<MusicianDto> {
        return musicianRepository.findAll()
                                 .map(MusicianDto::create)

    }

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return Musician
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long): Musician {
        return musicianRepository.findByIdOrThrow(id)
    }

    /**
     * 뮤지션 이름으로 뮤지션 정보를 가져온다.
     * @return Musician?
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByName(name: String): Musician? {
        return musicianRepository.findByName(name)
    }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode): List<Musician> {
        return musicianRepository.findByGenre(genre)
    }

}
```

하지만 오류가 발생한다. 오류 정보를 찾아보니 T?를 반환해서이다.

코틀린 문법에 기인하면 당연한 내용이기 때문에 그리고 생각해보면 null이면 에러를 반환할테니 non-null로 처리해야한다.      

```Kotlin
/**
 * null을 반환하지 않고 ObjectEmptyException에러를 날리자.
 */
fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T = this.findByIdOrNull(id) ?: throw ObjectEmptyException()
```

다음과 같이 처리를 하자.

하지만 우리가 작업을 하면서 생각해 볼 것은 앞으로 ?:을 이용해 무언가 후처리하는 많은 작업들을 할 소지가 많다는 것을 고민해야 한다.

findById는 그렇다치고 어떤 특정 조건으로 조회할때 그 정보가 unique result의 경우에는 Repository에서 T?같은 방식으로 반환할 수 있고 이것을 엘비스 연산자를 통해 차후 로직을 다룰 수 있다.

예를 들면 널이면 에러를 던지는 식이다.

결국 다음과 같은 에시로 코드 컨벤션이 난무할 소지가 많다.


```Kotlin
@Service
class 어떤서비스 {

    @Transactional(readOnly = true)
    fun doSearch(name: String): Any {
        return rep.soSearch(name) ?: throw ObjectEmptyException("조회된 정보가 없음")
    }

    @Transactional(readOnly = true)
    fun doSearch1(name: String): Any {
        return rep.soSearch1(name) ?: throw ObjectEmptyException("조회된 정보가 없음")
    }
}

```

그러면 어떻게 해볼까 ?

자바였다면 functional interface를 활용해 유틸 클래스에서 간단하게 처리하도록 만들었을 것이다.

그렇다면 코틀린에서는??


# 그에 앞서 코틀린은 유틸 클래스를 어떻게 만들까?

자바에서는 일반적으로 static이 붙은 정적 메소드를 담은 유틸 클래스를 만들어 사용하곤 한다.

이런 경우에는 이펙티브 자바에서도 소개되었지만 인스턴스 방지화를 하기 위한 몇가지가 소개가 되어 있고 자바의 경우에는 롬복을 이용해 간단하게 처리한다.

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MyUtils {

}
```
이런식으로 말이다.     

하자만 코틀린은 좀 자바스크립트스러운 면이 있다.

그냥 클래스 하나 만들고 어떤 클래스에도 포함되지 않은 메소드를 만들면 된다. 저기 위에서 확장 함수를 만든것처럼 말이다.

이제부터는 CommonUtils를 하나 만들어 볼까 한다.

그중에 우선 먼저 만들어 볼것은 반복될 소지가 있는 ObjectEmptyException()을 던지는 코드를 메소드로 만들어 볼 생각이다.

이것을 만들기 위해서 코틀린의 Any, Unit, Nothing에 대해서 어느 정도 알아야 한다.

특히 Nothing에 대한 설명을 살펴보면

[Kotlin Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing.html)

진짜 짤막하게 설명이 되어 있는데 주목할 부분은 마지막 글귀이다.

```
Nothing has no instances. 
You can use Nothing to represent "a value that never exists": for example, 
if a function has the return type of Nothing, 
it means that it never returns (always throws an exception).
```

그렇다면 에러를 던지는 경우에는 반환 타입을 Nothing으로 처리하면 될것이다.

```Kotlin

/**
 * 메세지가 없는 경우
 */
fun objectEmpty(): Nothing {
    throw ObjectEmptyException()
}

/**
 * 메세지가 있는 경우
 */
fun objectEmpty(message: String?): Nothing {
    if(message == null) {
        objectEmpty()
    } else {
        throw ObjectEmptyException(message)
    }
}

```
다음과 같이 메세지가 있는 경우와 없는 경우를 만든다.

CustomCrudRepositoryExtensions.kt

```Kotlin
/**
 * null을 반환하지 않고 OptionalEmptyException에러를 날리자.
 */
fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID, message: String? = null): T = this.findByIdOrNull(id) ?: objectEmpty(message)
```

메세지의 경우에는 위와 같이 표현하기 때문에 기존의 코드를 자바처럼 findByIdOrThrow(id, null)처럼 해주지 않아도 코틀린에서 알아서 처리해준다.

의도적으로 나는 다음과 같이

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
     * @return List<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(): List<MusicianDto> {
        return musicianRepository.findAll()
                                 .map(MusicianDto::create)

    }

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return Musician
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long, message: String? = null): Musician {
        return musicianRepository.findByIdOrThrow(id, message)
    }

    /**
     * 뮤지션 이름으로 뮤지션 정보를 가져온다.
     * @return Musician?
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByName(name: String): Musician? {
        return musicianRepository.findByName(name)
    }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<Musician>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode): List<Musician> {
        return musicianRepository.findByGenre(genre)
    }

}
```

하지만 기존의 만들어 놓은 테스트 코드에는

```Kotlin
    @Test
@DisplayName("아이디로 뮤지션 정보를 가져오는 메소드를 테스트한다.")
fun fetchMusicianById_TEST() {
    val musicianId = 1L
    val musician = musicianService.fetchMusicianById(musicianId)
    assertThat(musician.name).isEqualTo("Charlie Parker")

    val otherMusicianId = 1_111L
    val otherMusician = musicianService.fetchMusicianById(otherMusicianId, "어아다 ${otherMusicianId}로 조회된 뮤지션 정보가 없습니다. 아이디를 확인해 보세요.")
    assertThat(otherMusician.name).isEqualTo("Charlie Parker")

}
```

문제없이 돌아간다. 즉 코틀린에서 알아서 처리해 준다. 자바스크립트같은 느낌을 준다.      

최종적으로는 dto로 바꾸고 응답 객체에 담아서 보내보자.


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
     * @return List<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(): List<MusicianDto> {
        return musicianRepository.findAll()
                                 .map(MusicianDto::create)

    }

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return MusicianDto
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long, message: String? = null): MusicianDto {
        return musicianRepository.findByIdOrThrow(id, message)
                                 .let {MusicianDto.create(it)}
    }

    /**
     * 뮤지션 이름으로 뮤지션 정보를 가져온다.
     * @return MusicianDto?
     */
    @Transactional(readOnly = true)
    fun fetchMusicianByName(name: String): MusicianDto? {
        return musicianRepository.findByName(name)?.let { MusicianDto.create(it) }
    }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<MusicianDto>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode): List<MusicianDto> {
        return musicianRepository.findByGenre(genre)
                                 .map(MusicianDto::create)
    }

}

```

뮤지션 이름으로 조회시에는 없을 수 있기 때문에 null을 반환하도록 만들자.

findByName의 경우에는 테스트 코드에서 에러가 날것이다.


```Kotlin
    @Test
    @DisplayName("이름으로 뮤지션 리스트를 가져오는 메소드를 테스트한다.")
    fun fetchMusiciansByName_TEST() {
        val musicianName = "Charlie Parker"
        val musician = musicianService.fetchMusicianByName(musicianName)
        assertThat(musician?.name).isEqualTo("Charlie Parker")
    }
```
null이 올수 있기 때문에 다음과 같이 표현할 수 있다. thymeleaf를 해보신 분들이라면 상당히 익숙한 코드!!!!

ResponseResult의 정적 메소드에는 null을 받지 않도록 해놨는데 이름으로 조회시 null이 올수 있기 때문에 다음과 같이 변경하자.

```Kotlin
/**
 * Rest API response 정보를 담은 객체
 */
data class ResponseResult<T>(
    private val _result: T?,
) {

    val result get() = this._result

    companion object {
        /**
         * ResponseResult를 생성하는 정적 메소드
         * @param result
         * @return ResponseResult<T>
         */
        fun <T> of(result: T?): ResponseResult<T> {
            return ResponseResult(result)
        }
    }

}
```

이제는 컨트롤러를 수정하자!

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
    fun musicians() : ResponseResult<List<MusicianDto>> {
        return ResponseResult.of(musicianService.fetchMusicians())
    }

    @GetMapping("/musicians/{id}")
    fun musicianById(@PathVariable("id") id: Long) : ResponseResult<MusicianDto> {
        return ResponseResult.of(musicianService.fetchMusicianById(id))
    }

    @GetMapping("/musicians/genre/{genre}")
    fun musicianByName(@PathVariable("genre") genre: GenreCode) : ResponseResult<List<MusicianDto>> {
        return ResponseResult.of(musicianService.fetchMusiciansByGenre(genre))
    }

    @GetMapping("/musician/name/{name}")
    fun musicianByName(@PathVariable("name") name: String) : ResponseResult<MusicianDto> {
        return ResponseResult.of(musicianService.fetchMusicianByName(name))
    }



}

```

포스트맨이나 브라우저 또는 사용하는 API툴을 사용해서 한번 날려보자.

# more more concisely

CrudRepositoryExtensions.kt 나 CustomCrudRepositoryExtensions.kt의 코드를 잘 살펴보면 독특함이 느껴진다.

일반적으로 우리는 어떤 메소드가 실행되고 난 이후 반환값이 있다면 return을 통해 반환한다.

```Kotlin
fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T? = this.findByIdOrNull(id) ?: throw ObjectEmptyException()
```

자바였다면

```Java
public String soSomething() {
    return "test";
}
```

하지만 위와 같은 코드를 보면 '=' 이후 코드 블락 자체가 그냥 반환된다.

이것은 다른 곳에서도 적용할 수 있는데 간결하게 표현할 수 있는 부분은 전부 변경해 보자.

GenreCode.kt
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
        fun of(genre: String?): GenreCode = values().firstOrNull { genreEnum -> genreEnum.genre.equals(genre, ignoreCase = true) }
                                            ?: throw IllegalArgumentException("맞는 장르 코드가 없습니다. 장르 코드를 확인하세요.")
    }

}
```

ResponseResult.kt
```Kotlin
/**
 * Rest API response 정보를 담은 객체
 */
data class ResponseResult<T>(
    private val _result: T?,
) {

    val result get() = this._result

    companion object {
        /**
         * ResponseResult를 생성하는 정적 메소드
         * @param result
         * @return ResponseResult<T>
         */
        fun <T> of(result: T?) = ResponseResult(result)
    }

}
```

MusicianService.kt
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
     * @return List<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians() = musicianRepository.findAll()
                                             .map(MusicianDto::create)

    /**
     * 뮤지션 아이디로 해당 뮤지션 정보를 가져온다.
     * @return MusicianDto
     */
    @Transactional(readOnly = true)
    fun fetchMusicianById(id: Long, message: String? = null) = musicianRepository.findByIdOrThrow(id, message)
                                                                                 .let {MusicianDto.create(it)}

    /**
     * 뮤지션 이름으로 뮤지션 정보를 가져온다.
     * @return MusicianDto?
     */
    @Transactional(readOnly = true)
    fun fetchMusicianByName(name: String) = musicianRepository.findByName(name)?.let { MusicianDto.create(it) }

    /**
     * 장르로 이에 해당하는 뮤지션 리스트를 가져온다.
     * @return List<MusicianDto>
     */
    @Transactional(readOnly = true)
    fun fetchMusiciansByGenre(genre: GenreCode) = musicianRepository.findByGenre(genre)
                                                                    .map(MusicianDto::create)

}
```

MusicianController.kt
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

    /**
     * 뮤지션 리스트를 반환한다.
     * @return ResponseResult<List<MusicianDto>>
     */
    @GetMapping("/musicians")
    fun musicians() = ResponseResult.of(musicianService.fetchMusicians())

    /**
     * 아이디로 뮤지션을 조회한다.
     * @param id: Long
     * @return ResponseResult<MusicianDto>
     */
    @GetMapping("/musicians/{id}")
    fun musicianById(@PathVariable("id") id: Long) = ResponseResult.of(musicianService.fetchMusicianById(id))

    /**
     * 장르로 뮤지션 리스트를 반환한다.
     * @param genre: GenreCode
     * @return ResponseResult<List<MusicianDto>>
     */
    @GetMapping("/musicians/genre/{genre}")
    fun musicianByName(@PathVariable("genre") genre: GenreCode) = ResponseResult.of(musicianService.fetchMusiciansByGenre(genre))

    /**
     * 뮤지션 이름으로 조회한다. 단 없으면 null이 반환된다.
     * @param name: String
     * @return ResponseResult<MusicianDto>
     */
    @GetMapping("/musician/name/{name}")
    fun musicianByName(@PathVariable("name") name: String) = ResponseResult.of(musicianService.fetchMusicianByName(name))

}
```
타입 추론이 가능해져서 위와 같이 표현이 가능하다.      

테스트를 해봐도 동일한 결과를 얻을 수 있다.     

이전 브랜치에 작성한 코드와 한번 비교해 보시길 바란다.

# At a Glance

기본적인 조회용 API를 작성했다.

다음 브랜치에서는 create/update를 하는 API와 기존의 모든 뮤지션을 가져오는 API를 페이징처리를 적용해 해볼 생각이다. 👏