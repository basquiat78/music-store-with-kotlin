# Ablum 작업을 해보자.

시작전에 album정보를 가져오는 로직은 앞서 작업한 것과 큰 차이가 없다.

다만 Album의 경우에는 Musician과의 관계에서는 @ManyToOne을 가질 수 있다.

그런데 분명 Lazy로 설정했음에도 Eager처럼 동작을 해서 찾아봤더니...

처음에 설명했던 allOpen을 설정해줘야 한다.

결국 착각이였던 것인가?

새로 설정한 build.gradle을 확인해 보길 바란다.

```sql

CREATE TABLE `album` (
  `id` int NOT NULL AUTO_INCREMENT,
  `musician_id` int NOT NULL,
  `title` varchar(200) NOT NULL,
  `album_type` enum('FULL','SINGLE','EP') NOT NULL,
  `rep` enum('y', 'n') NOT NULL COMMENT '음반이 해당 뮤지션의 대표 음반인지 나타내는 컬럼',
  PRIMARY KEY (`id`),
  CONSTRAINT `ALBUM_FK` FOREIGN KEY (`musician_id`) REFERENCES `musician` (`id`) ON DELETE CASCADE
) COMMENT='앨범 초간단 정보를 담는 테이블';

INSERT INTO basquiat.album (musician_id,title,album_type,rep) VALUES
	 (1,'Now Is The Time','FULL','y'),
	 (2,'Now Is The Time','FULL','n'),
	 (1,'With String','FULL','y');
```
체크용 데이터도 밀어넣자.

# Fetch Join Or @EntityGraph

이런 시나리오가 가능하다.

앨범을 조회할 때 (어떤 api를 호출할때) 앨범 정보와 뮤지션의 정보를 같이 보내줄 때 발생할 수 있는 상황이 있다.

악명이 자자한 N+1이다.

그러기 위해서는 Fetch Join을 고려해 볼 수 있다.

```Kotlin
    //@EntityGraph(attributePaths = ["musician"])
@Query("SELECT album FROM Album AS album JOIN FETCH album.musician")
fun findByTitle(title: String) : List<Album>
```
하지만 저렇게 작업하는 것이 여간 귀찮은 것이 아니다. 그래서 활용할 수 있는 것이 EntityGraph가 있다.

다만 이 두개는 큰 차이가 존재한다.

```
// Fetch Join을 사용할 때
Hibernate: 
    /* SELECT
        album 
    FROM
        Album AS album 
    JOIN
        FETCH album.musician */ select
            album0_.id as id1_0_0_,
            musician1_.id as id1_1_1_,
            album0_.album_type as album_ty2_0_0_,
            album0_.musician_id as musician5_0_0_,
            album0_.rep as rep3_0_0_,
            album0_.title as title4_0_0_,
            musician1_.genre as genre2_1_1_,
            musician1_.name as name3_1_1_ 
        from
            album album0_ 
        inner join
            musician musician1_ 
                on album0_.musician_id=musician1_.id


// EntityGraph를 사용할 때
Hibernate: 
    /* select
        generatedAlias0 
    from
        Album as generatedAlias0 
    where
        generatedAlias0.title=:param0 */ select
            album0_.id as id1_0_0_,
            musician1_.id as id1_1_1_,
            album0_.album_type as album_ty2_0_0_,
            album0_.musician_id as musician5_0_0_,
            album0_.rep as rep3_0_0_,
            album0_.title as title4_0_0_,
            musician1_.genre as genre2_1_1_,
            musician1_.name as name3_1_1_ 
        from
            album album0_ 
        left outer join
            musician musician1_ 
                on album0_.musician_id=musician1_.id 
        where
            album0_.title=?
```

차이점이 보이나? INNER JOIN과 LEFT OUTER JOIN의 차이가 있다.

실제로 이와 관련 정보를 찾아보면 성능의 문제를 크게 꼽는다.

얼마나 큰 성능의 차이가 있는지 나의 경우에는 잘 모르겠지만 내가 아는 몇몇 DBA분들이 하나같이 하는 말이 있다.

"거 왠간하면 INNER JOIN 사용해라. 데이터가 많거나 또는 컬럼이 많은 경우에는 성능의 차이가 난다."

그렇다고 한다.

실제로 지금같은 케이스는 LEFT OUTER JOIN을 사용할 이유가 전혀 없다. 왜냐하면 뮤지션의 정보가 없는 앨범이 존재할 수 있을까???

따라서 LEFT OUTER JOIN을 사용하는 것은 확실히 리소스 낭비라는 것이다.

하지만 그런 성능은 충분히 커버 가능하고 간결함을 추구한다면 @EntityGraph를 사용하는 것도 좋은 선택이다.

다만 이것은 차후 후에 구현할 queryDSL을 사용하게 되면 쉽게 처리할 수 있기 때문에 지금은 @EntityGraph를 사용할 생각이다.

# Fetch Join의 한계

이게 무슨 말이냐????

```Kotlin
    @Query("SELECT a FROM Album AS a WHERE a.musician.id = :musicianId")
fun findByMusicianId(musicianId: Long, pageable: Pageable) : Page<Album>
```
이런 코드가 있다고 보자.

의도는 명확하다. 어떤 특정 뮤지션의 앨범 정보를 페이징처리해서 가져오겠다는 것이다.

하지만 이것은 만일 뮤지션의 정보를 dto에 담겠다고 musician에 접근하는 순간 N+1에 걸린다.

그렇다면 이런 생각을 할 수 있을 것이다.
```Kotlin
    @Query("SELECT album FROM Album AS album JOIN FETCH album.musician AS m WHERE m.id = :musicianId")
fun findByMusicianId(musicianId: Long, pageable: Pageable) : Page<Album>
```
바로 에러를 만나게 된다.

이게 하이버네이트를 사용할 때는 가능하다고 하는데 Spring JPA에서는 에러를 발생한다.

이와 관련 여러 내용을 StackOverFlow나 김영한님의 책에서도 알 수 있다.

왜냐하면 fetch join의 결과는 연관된 모든 엔티티가 있을것이라 가정한다. 결국 데이터의 정합성과 관련된 문제라는 것이다.

그래서 FETCH JOIN의 대상이 되는 album.musician을 ON이나 WHERE 절에서 사용하면 안된다는 것이다.

이런 경우라면 차라리 뮤지션의 정보는 어짜피 한명이니 쿼리로 해당 뮤지션의 정보를 가져오는게 나을 수 있다.

그리고 findByMusicianId를 통해 가져온 앨범 정보만 사용하는 방식으로 구현할 수 있다.

그렇지 않다면 queryDSL을 통해서 좀 더 다른 방식을 선택할 수 있는데 이 방법은 나중에 최종적으로 과정이 끝난 이후 해볼 생각이다.

# 최종적인 조회용 API 작성

Album.kt
```Kotlin
/**
 * created by basquiat
 */
@Entity
@Table(name = "album")
class Album(

    @Enumerated(EnumType.STRING)
    var rep: YesOrNo? = YesOrNo.n,

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type")
    var albumType: AlbumType? = AlbumType.FULL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id")
    var musician: Musician,

    var title: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    ) {
    init {
        if(title.isBlank()) {
            throw MandatoryArgumentException("앨범 명은 필수입니다.")
        }
    }
}
```

AlbumDto.kt

```Kotlin
/**
 * AlbumDto
 * created by basquiat
 */
data class AlbumDto(

    val id: Long? = null,

    val musicianName: String,

    val title: String,

    @JsonProperty("albumType")
    private var _albumType: AlbumType,
) {
    /**
     * 클라이언트로 정보를 내려줄 때는 albumType의 설명을 보내주기 위해 backing property를 사용
     */
    val albumType: String get() = this._albumType.description

    companion object {
        /**
         * @param album
         * @param musician
         * @return AlbumDto
         */
        fun createWithMusician(album: Album, musician: MusicianDto) = with(album) {
            AlbumDto(title = title,
                id = id,
                _albumType = albumType?: AlbumType.FULL,
                musicianName = musician.name
            )
        }

        /**
         * @param album
         * @return AlbumDto
         */
        fun create(album: Album) = with(album) {
            AlbumDto(title = title,
                id = id,
                _albumType = albumType?: AlbumType.FULL,
                musicianName = musician.name
            )
        }
    }

}
```

AlbumRepository는 다음과 같이 작업을 했다.

```Kotlin
/**
 * AlbumRepository
 */
interface AlbumRepository : BaseRepository<Album, Long> {

    @Query("SELECT a FROM Album AS a JOIN FETCH a.musician",
        countQuery = "SELECT COUNT(a) FROM Album AS a")
    fun fetchAllAlbum(pageable: Pageable) : Page<Album>

    @Query("SELECT a FROM Album AS a WHERE a.musician.id = :musicianId")
    fun findByMusicianId(musicianId: Long, pageable: Pageable) : Page<Album>

    @EntityGraph(attributePaths = ["musician"])
    //@Query("SELECT album FROM Album AS album JOIN FETCH album.musician")
    fun findByTitle(title: String, pageable: Pageable) : Page<Album>

}
```
fetchAllAlbum의 경우에는 메소드 쿼리로 만드는 것이 아닌 새로운 메소드이기 때문에 Pageable처리시 countQuery를 작성해줘야 한다.

findByTitle은 위에서 언급했던 것처럼 @EntityGraph로 작업을 해 본 것이다.

서비스 레이어는 필요에 맞게 나의 생각대로 작성을 해봤다. 이 부분은 개발자의 상황이나 더 좋은 방법이 있다면 그 방식을 택하면 된다.

여기서는 Kotlin에 집중하고자 한다.


```Kotlin

/**
 * 앨범 정보를 다루는 서비스 객체
 * created by basquiat
 */
@Service
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val musicianService: MusicianService,
) {

    /**
     * 앨범 리스트를 가져온다.
     * @return Page<AlbumDto> 앨범 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(pageable: Pageable): Page<AlbumDto> = albumRepository.fetchAllAlbum(pageable)
        .map(AlbumDto::create)

    /**
     * 특정 뮤지션의 앨범 리스트를 가져온다.
     * @return Page<AlbumDto> 앨범 리스트
     */
    @Transactional(readOnly = true)
    fun fetchAlbumsByMusicianId(musicianId: Long, pageable: Pageable): Page<AlbumDto> {
        val musician = musicianService.fetchMusicianById(musicianId)
        return albumRepository.findByMusicianId(musicianId, pageable)
            .map { entity -> AlbumDto.createWithMusician(entity, musician) }
    }


    /**
     * 타이틀로 앨범 리스트를 가져온다.
     * 동명 타이틀의 음반이 많을 수 있기 때문에 리스트로 가져온다.
     * @return Page<AlbumDto> 앨범 리스트
     */
    @Transactional(readOnly = true)
    fun fetchAlbumsByTitle(title: String, pageable: Pageable): Page<AlbumDto> = albumRepository.findByTitle(title, pageable)
        .map(AlbumDto::create)
}
```

```Kotlin
/**
 * 앨범 정보와 관련된 컨트롤러
 * created by basquiat
 */
@RestController
@RequestMapping("/api/music/store")
class AlbumController(
    private val albumService: AlbumService,
) {

    /**
     * 앨범 리스트를 반환한다.
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums")
    fun musicians(pagination: Pagination): ResponseResult<List<AlbumDto>> {
        val albumsWithPage = albumService.fetchMusicians(PageRequest.of(pagination.offset, pagination.limit))
        with(albumsWithPage) {
            pagination.totalCount = totalElements
            pagination.totalPage = totalPages
            pagination.last = isLast
        }
        return ResponseResult.of(albumsWithPage.content, pagination)
    }

    /**
     * 뮤지션의 앨범 리스트를 가져온다.
     * @param musicianId: Long
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums/musician/{id}")
    fun albumsByMusicianId(@PathVariable("id") musicianId: Long, pagination: Pagination): ResponseResult<List<AlbumDto>> {
        val albumsWithPage = albumService.fetchAlbumsByMusicianId(musicianId, PageRequest.of(pagination.offset, pagination.limit))
        with(albumsWithPage) {
            pagination.totalCount = totalElements
            pagination.totalPage = totalPages
            pagination.last = isLast
        }
        return ResponseResult.of(albumsWithPage.content, pagination)
    }

    /**
     * 앨범명에 해당하는 앨범 리스트를 가져온다.
     * @param title: String
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums/title/{title}")
    fun albumsByTitle(@PathVariable("title") title: String, pagination: Pagination): ResponseResult<List<AlbumDto>> {
        val albumsWithPage = albumService.fetchAlbumsByTitle(title, PageRequest.of(pagination.offset, pagination.limit))
        with(albumsWithPage) {
            pagination.totalCount = totalElements
            pagination.totalPage = totalPages
            pagination.last = isLast
        }
        return ResponseResult.of(albumsWithPage.content, pagination)
    }

}
```

다만 여기서 컨트롤러의 경우에는 사실 변경점이 필요하다. 하지만 그대로 진행할 것이다. 차후 이 부분은 좀 더 아름답게 변경할 생각이다.

# 뭔가 걸리는게 있다.!

Album 테이블에 rep라는 컬럼이 있다. enum으로 설정해놨는데 y,n으로 소문자를 가지고 있다.

재미있겠도 이것은 Y/N으로 들어올 경우 에러가 발생한다.


대소문자를 구분하기 때문인데 그런 이유로 우리는 다음과 자주 사용할 수 있는 부분이라 common패키지에

```Kotlin
/**
 * YesOrNo enum
 * created by basuqiat
 */
enum class YesOrNo {
    y,
    n
}
```
처럼 작업을 해 놨다. 하지만 여러분이 sonarLint를 사용하게 되면 상수의 변수명에 대해

```
With the default regular expression ^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$:
```
이런 문구를 보게 된다.

대문자로 작성하라는 의미인데 그렇다면 이제부터 이것을 convert를 활용해 이것을 처리해 보자.

로직내에서는 상관이 없을 수 있지만 디비로부터 null인 경우가 들어있는 경우를 체크하기 위해 방어적인 인터페이스를 하나 만든다.

```Kotlin
/**
 * enum class에 대해서 generic하게 컨버터를 사용하기 위한 인터페이스로
 * enum의 상수값을 가져온다.
 * created by basquiat
 */
interface InterfaceGenericEnum<out T> {
    fun defaultIfNull(): T
}
```

기존의 내가 만들었던 자바의 converter는 다음과 같다.

```java
/**
 * interfaceGenericEnum을 위한 추상 클래스
 * created by basquiat
 */
@Converter
@RequiredArgsConstructor
public abstract class LowCaseEnumConverter<T extends Enum<T> & InterfaceGenericEnum> implements AttributeConverter<T, String> {

    private final Class<T> clazz;

    /**
     * enum 상수 값을 가져와서 lowerCase로 반환한다.
     * @param attribute
     * @return String
     */
    @Override
    public String convertToDatabaseColumn(T attribute) {
        if(attribute == null) {
            T[] enums = clazz.getEnumConstants();
            attribute = Arrays.stream(enums)
                    .filter(en -> en == en.defaultIfNull())
                    .findFirst().orElseThrow(NoSuchElementException::new);
        }
        return attribute.name().toLowerCase();
    }

    /**
     * 디비 정보는 lowerCase로 upperCase로 변환후 비교후 해당 enum객체를 반환하게 한다.
     * @param dbData
     * @return T
     */
    @Override
    public T convertToEntityAttribute(String dbData) {
        T[] enums = clazz.getEnumConstants();
        try {
            return Arrays.stream(enums)
                    .filter(en -> en.name().equals(dbData.toUpperCase()))
                    .findFirst().orElseThrow(NoSuchElementException::new);
        } catch(NullPointerException e) {
            return defaultEnum(enums);
        }
    }

    /**
     * 반복되는 공통 코드 줄이자
     * @param enums
     * @return T
     */
    private T defaultEnum(T[] enums) {
        return Arrays.stream(enums)
                .filter(en -> en == en.defaultIfNull())
                .findFirst().orElseThrow(NoSuchElementException::new);
    }

}
```


```Kotlin
/**
 * interfaceGenericEnum을 위한 추상 클래스
 * created by basquiat
 */
@Converter
abstract class LowCaseEnumConverter<T: Enum<T>> protected constructor(private val clazz: Class<T>) :
    AttributeConverter<T, String> where T: InterfaceGenericEnum<out T> {

    /**
     * enum 상수 값을 가져와서 lowerCase로 반환한다.
     * @param attribute
     * @return String
     */
    override fun convertToDatabaseColumn(attribute: T): String {
        var attribute: T? = attribute
        if (attribute == null) {
            val enums = clazz.enumConstants
            attribute = enums.first{ en -> en == en.defaultIfNull() }
                ?: throw IllegalArgumentException("해당 컬럼의 db enum값과 해당 enum클래스의 정보가 맞지 않습니다. 확인하세요.")
        }
        return attribute.name.lowercase()
    }

    /**
     * 디비 정보는 lowerCase로 upperCase로 변환후 비교후 해당 enum객체를 반환하게 한다.
     * @param dbData
     * @return T
     */
    override fun convertToEntityAttribute(dbData: String): T {
        val enums = clazz.enumConstants
        return try {
            enums.first{ en -> en.name == dbData.uppercase() }
                ?: throw IllegalArgumentException("해당 컬럼의 db enum값과 해당 enum클래스의 정보가 맞지 않습니다. 확인하세요.")
        } catch (e: NullPointerException) {
            defaultEnum(enums)
        }
    }

    /**
     * 반복되는 공통 코드 줄이자
     * @param enums
     * @return T
     */
    private fun defaultEnum(enums: Array<T>): T {
        return enums.first{ en -> en == en.defaultIfNull() }
    }
}
```

나머지 코드들은 대부분 비슷하거나 간결하게 표현되는 것은 알 수 있다.

하지만 제너릭과 관련해서는 코틀린의 경우에는 좀 다르다.

'LowCaseEnumConverter<T extends Enum<T> & InterfaceGenericEnum>'이 코드를 보면 해당 제너릭에 구현된 인터페이스를 &연산자를 통해서 알려주는 방식을 택하고 있다.

하지만 코틀린에서는 Top레벨로 들어오는 제너릭에 대해서 where라는 키워드를 사용한다.

'where T: InterfaceGenericEnum<out T>', 즉 이 코드는 T에 대해서 해당 인터페이스가 구현되어 있다는 것을 알려준다.

[baeldung Kotlin Generic](https://www.baeldung.com/kotlin/generics)

참고하면 될것 같다.

최종적으로 YesOrNo.kt는 다음과 같이 변경된다.

```Kotlin
/**
 * YesOrNo enum
 * created by basuqiat
 */
enum class YesOrNo : InterfaceGenericEnum<YesOrNo> {

    Y,
    N;

    /**
     * DB에서 어떤 이유로 null인 경우도 있기 때문에 null일 경우 기본적으로 넘겨줄 enum객체를 생성한다.
     * @return YesOrNo
     */
    override fun defaultIfNull() = N

    /**
     * Custom Converter for low case
     */
    class LowCaseConverter : LowCaseEnumConverter<YesOrNo>(YesOrNo::class.java)

}
```

```Kotlin
/**
 * created by basquiat
 */
@Entity
@Table(name = "album")
class Album(

    @Convert(converter = YesOrNo.LowCaseConverter::class)
    var rep: YesOrNo? = YesOrNo.N,

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type")
    var albumType: AlbumType? = AlbumType.FULL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id")
    var musician: Musician,

    var title: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    ) {
    init {
        if(title.isBlank()) {
            throw MandatoryArgumentException("앨범 명은 필수입니다.")
        }
    }
}
```

컨트롤러에 페이징 정보를 만드는 부분이 반복되는 것이 눈에 거슬린다.

```Kotlin
fun <T> setPagination(pagination: Pagination, page: Page<T>) {
    with(page) {
        pagination.totalCount = totalElements
        pagination.totalPage = totalPages
        pagination.last = isLast
    }
}
```

```Kotlin
/**
 * 앨범 정보와 관련된 컨트롤러
 * created by basquiat
 */
@RestController
@RequestMapping("/api/music/store")
class AlbumController(
    private val albumService: AlbumService,
) {

    /**
     * 앨범 리스트를 반환한다.
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums")
    fun musicians(pagination: Pagination): ResponseResult<List<AlbumDto>> {
        val albumsWithPage = albumService.fetchMusicians(PageRequest.of(pagination.offset, pagination.limit))
        setPagination(pagination, albumsWithPage)
        return ResponseResult.of(albumsWithPage.content, pagination)
    }

    /**
     * 뮤지션의 앨범 리스트를 가져온다.
     * @param musicianId: Long
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums/musician/{id}")
    fun albumsByMusicianId(@PathVariable("id") musicianId: Long, pagination: Pagination): ResponseResult<List<AlbumDto>> {
        val albumsWithPage = albumService.fetchAlbumsByMusicianId(musicianId, PageRequest.of(pagination.offset, pagination.limit))
        setPagination(pagination, albumsWithPage)
        return ResponseResult.of(albumsWithPage.content, pagination)
    }

    /**
     * 앨범명에 해당하는 앨범 리스트를 가져온다.
     * @param title: String
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums/title/{title}")
    fun albumsByTitle(@PathVariable("title") title: String, pagination: Pagination): ResponseResult<List<AlbumDto>> {
        val albumsWithPage = albumService.fetchAlbumsByTitle(title, PageRequest.of(pagination.offset, pagination.limit))
        setPagination(pagination, albumsWithPage)
        return ResponseResult.of(albumsWithPage.content, pagination)
    }

}
```

# At a Glance

기본적인 앨범 조회용 API와 JPA와 관련된 몇가지 잡지식과 제너릭에 대해서 어느정도 소개했다.

약간은 어설픈 컨트롤러와 컨셉이긴 하지만 차후에 리팩토링을 통해 좀 더 멋지게 변경해 볼 생각이다.

다음 브랜치는 역시 앨범에 대한 create/update에 대한 내용을 담을 예정이다.        