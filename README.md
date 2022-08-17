# 뮤지션 생성 및 업데이트 해보기

저번 브랜치에서는 몇가지 조회 API를 만들어 봤다.

물론 실업무나 프로덕트단계의 어플리케이션은 이보다 더 복잡할 수 있겠지만 컨셉 자체가 변경된 것이 없다.

다만 사용하는 언어가 자바에서 코틀린으로 변경된 것이기 때문에 이정도로만으로도 코틀린을 통해서 어떻게 작업을 해나갈지 충분할 것이다.

그렇다면 이제는 조회가 아닌 필요한 데이터를 생성하거나 변경된 부분이 있을 경우 변경할 수 있는 API가 필요하다.

# VO를 만들어보자.

클라이언트로부터 뮤지션을 생성할때 필요한 정보를 먼저 만들 필요가 있다.

```Kotlin
/**
 * 뮤지션 생성 및 변경을 위한 request 객체
 */
data class MusicianRequest(
    val name: String,
    val genre: GenreCode
)
```

그리고 이 객체를 받아서 실제 디비로 뮤지션 정보를 생성하는 메소드를 만들어 보자.

```Kotlin
    /**
     * 뮤지션 정보를 db에 생성한다.
     * @return Musician
     */
    @Transactional
    fun createMusician(request: MusicianRequest): Musician {
        val createMusician = with(musicianRequest){Musician(name = name, genre = genre)}
        musicianRepository.save(createMusician)
        return createMusician
    }
```
실제로 Musican을 반환해도 되고 반환하지 않아도 상관없다.


```Kotlin
    @Test
    @DisplayName("뮤지션 정보를 db에 인서트성한다.")
    fun createMusician_TEST() {
        val createdMusician = musicianService.createMusician(MusicianRequest("Miles Davis", GenreCode.JAZZ))
        assertThat(createdMusician.name).isEqualTo("Miles Davis")

        val musicians = musicianService.fetchMusicians()
        assertThat(musicians).hasSize(3)
    }
```
기존에 2개의 정보가 DB에 들어있고 새로운 뮤지션이 생성되었으니 전체 뮤지션 정보를 가져오면 3개의 row가 반환될테니 이 테스트는 통과할 것이다.

컨트롤러에도 관련 메소드를 만들어 보자.


```Kotlin
    /**
     * 뮤지션 정보를 생성한다.
     */
    @PostMapping("/musicians")
    fun createMusician(@RequestBody request: MusicianRequest) {
        musicianService.createMusician(request)
    }
```

포스트맨을 통해서 테스트해보자. 여기서는 반환 객체를 설정하지 않았지만 제대로 생성되고 아이디가 반환이 되는지 보고 싶다면 생성된 뮤지션 정보를 반환해도 무방하다.

나의 경우에는 포스트맨을 통해서 하나의 뮤지션 정보를 하나 더 밀어 넣었다.

따라서 현재 musician 테이블에서는 총 4개의 row가 생성되어 있다.

이제는 업데이트를 해보자.

크게 달라질 것이 없을 것이다.

다만 여기서는 dirty checking을 확인해 보기 위해서 이름만 변경하는 메소드를 만들 것이다.

만일 이름과 장르가 잘못되었다면 JPA를 사용하기 때문에 save를 통해서 전체 객체로 넘겨 변경할 수 있다.

변경 요청을 위한 객체를 따로 만들 수 있지만 여기서는 그냥 기존에 만들어 놓은 MusicianRequest를 그대로 활용할 생각이다.

기존에 만든 MusicianRequest은 다음과 같이 변경될 수 있다.

```Kotlin
/**
 * 뮤지션 생성 및 변경을 위한 request 객체
 */
data class MusicianRequest(
    val name: String,
    val genre: GenreCode? = GenreCode.ETC,
    val id: Long? = null
)
```

이제는 업데이트 메소드를 생성한다.

```Kotlin
    /**
     * 뮤지션 정보를 변경한다.
     */
    @Transactional
    fun updateMusician(request: MusicianRequest) {
        val selectMusician = musicianRepository.findByIdOrThrow(request.id)
        selectMusician.name = request.name
    }
```

물론 저렇게 접근하지 않고 명확하게 뮤지션의 이름을 변경한다는 메소드를 엔티티에 만들어서 호출하는 방법도 좋다.

하지만 여기서는 그냥 코드를 추가하지 않고 심플하게 갈것이다.

이제 테스트를 해보자.

```Kotlin
    @Test
    @DisplayName("뮤지션 정보를 변경한다.")
    fun updateMusician_TEST() {
        // Miles Davis에서 Bud Powell로 변경한다.
        musicianService.updateMusician(MusicianRequest(id = 3, name = "Bud Powell"))

        val updatedMusician = musicianService.fetchMusicianById(3)
        assertThat(updatedMusician.name).isEqualTo("Bud Powell")
    }
```

컨트롤러도 메소드를 추가하자.

```Kotlin
    /**
     * 뮤지션 정보를 변경하고 변경된 정보를 담아서 보내준다.
     * @param request: MusicianRequest
     * @return ResponseResult<MusicianDto>
     */
    @PatchMapping("/musicians")
    fun updateMusician(@RequestBody request: MusicianRequest): ResponseResult<MusicianDto> {
        val musicianId = request.id ?: mandatoryParam("변경할 뮤지션의 id는 필수 입니다.");
        musicianService.updateMusician(request)
        return ResponseResult.of(musicianService.fetchMusicianById(musicianId))
    }
```

이제는 포스트맨을 통해서 뮤지션의 이름을 변경해 보자. 변경한 이후 변경된 뮤지션 정보가 제대로 반환될 것이다.


## 기존의 fetchMusicians를 페이징처리를 해보자.

리스트로 보여주는 정보의 경우에는 화면에서 페이징처리를 통해서 보여줘야 한다.

지금이야 몇개의 데이터가 없기 때문에 가능하지만 데이터가 많아지고 하게 되면 모든 데이터를 다 불러오는 것은 상당한 리소스가 든다.

다음과 같이 변경하자.

기본적으로 레파지토리는 처음 작성할 때 템플릿 인터페이스처럼 활용하고 있는 BaseRepository가 이것을 해결해 주기에 다음과 같이 작성하고

```Kotlin
    /**
     * 뮤지션 리스트를 반환한다.
     * @return ResponseResult<List<MusicianDto>>
     */
    @GetMapping("/musicians")
    fun musicians(@RequestParam("page", defaultValue = "0") page: Int, @RequestParam("size", defaultValue = "10") size: Int): ResponseResult<Page<MusicianDto>> {
        return ResponseResult.of(musicianService.fetchMusicians(PageRequest.of(page, size)))
    }

```

서비스쪽에서도

```Kotlin
    /**
     * 뮤지션의 정보를 가져온다.
     * @return Page<MusicianDto> 뮤지션 리스트
     */
    @Transactional(readOnly = true)
    fun fetchMusicians(pageable: Pageable?): Page<MusicianDto> = musicianRepository.findAll(pageable)
                                                                                   .map(MusicianDto::create)
```

이렇게 해주면 된다.     

Pageable에서도 map을 제공한다. 따라서 기존의 dto로 변경하는 코드는 변경이 필요없다.      

[page 0](localhost:8081/api/music/store/musicians?page=0&size=2)

[page 1](localhost:8081/api/music/store/musicians?page=1&size=2)

기본적으로 page는 1이 아닌 0부터 시작한다.

일단 날려보면 다음과 같이

```json
// localhost:8081/api/music/store/musicians?page=0&size=2
{
    "result": {
        "content": [
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
        ],
        "pageable": {
            "sort": {
                "sorted": false,
                "unsorted": true,
                "empty": true
            },
            "pageNumber": 0,
            "pageSize": 2,
            "offset": 0,
            "paged": true,
            "unpaged": false
        },
        "totalPages": 2,
        "totalElements": 4,
        "last": false,
        "numberOfElements": 2,
        "size": 2,
        "first": true,
        "number": 0,
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "empty": false
    }
}

// localhost:8081/api/music/store/musicians?page=1&size=2
{
    "result": {
        "content": [
            {
                "id": 3,
                "name": "Charles Mingus",
                "genre": "Jazz"
            },
            {
                "id": 4,
                "name": "Thelonious Monk",
                "genre": "Jazz"
            }
        ],
        "pageable": {
            "sort": {
                "sorted": false,
                "unsorted": true,
                "empty": true
            },
            "pageNumber": 1,
            "pageSize": 2,
            "offset": 2,
            "paged": true,
            "unpaged": false
        },
        "totalPages": 2,
        "totalElements": 4,
        "last": true,
        "numberOfElements": 2,
        "size": 2,
        "first": false,
        "number": 1,
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "empty": false
    }
}

```
하지만 이 방식으로는 뭔가 번잡한 느낌을 준다.

또한 page의 경우에는 0부터 시작하는데 클라이언트에서는 이것을 헛갈리게 하지 않기 위해서 1부터 받게 만들 것이다.

그리고 content와 페이지 정보중 필요한 부분만 생성해서 보내주고자 한다.

```Kotlin
/**
 * pagination
 */
data class Pagination(
    private var page: Int? = 0,
    private var size: Int? = 10,
    var last: Boolean? = false,
    var totalPage: Int? = 0,
    var totalCount: Long? = 0,
    private var _currentPage: Int? = 0
) {

    val offset : Int get() = this.page!! - 1
    val limit  : Int get() = this.size!!
    val currentPage: Int get() = this.page!!

    /**
     * 0이거나 0보다 작으면 기본 값을 세팅해준다.
     */
    init {
        if(page!! <= 0) {
            this.page = 1
        }

        if(size!! <= 0) {
            this.size = 10
        }
    }
}
```
위와  같이 Pagination을 하나 만든다.

이것을 통해서 요청시 또는 응답시 필요한 정보를 내려줄 생각이다.

페이징 처리를 클라이언트에서 어떻게 하느냐에 따라 보내줄 정보가 다를텐데 나의 경우에는 다음 정보만 필요하다.

현재 요청한 페이지, 전체 카운트 수, 전체 페이지 수, 그리고 현재 요청한 부분이 마지막 페이지정보인지만 보내줄 생각이다.

첫번째 페이지는 offset정보가 0이라면 첫번째 페이지일테니!

나는 Pagination을 만들어 사용하고 있지만 여러분이 사용하는 방식에 맞춰서 변경하면 된다.

회사마다 다르기도 하고 어떤 곳은 dataTables같은 녀석들과 연계헤서 사용하기 위한 다양한 방식을 선택하기도 하기 때문이다.

물론 사용하는 방식에 따라 이 방식을 고려해볼 수 있고 그냥 page의 정보를 그대로 활용할 수도 있다.

즉 이 방법은 상황에 따라 커스텀해서 사용하면 될것이다.

그리고 응답 객체도 변경해야 한다.

```Kotlin
/**
 * Rest API response 정보를 담은 객체
 */
@JsonPropertyOrder("result", "pagination")
data class ResponseResult<T>(
    private val _result: T?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val pagination: Pagination?
) {

    val result get() = this._result

    companion object {
        /**
         * ResponseResult를 생성하는 정적 메소드
         * @param result
         * @return ResponseResult<T>
         */
        fun <T> of(result: T?, pagination: Pagination? = null) = ResponseResult(result, pagination)
    }

}
```

페이징 정보가 없는 경우도 있고 있는 경우도 있기 때문에 null이라면 응답 정보에서 보여주지 않게 JsonInclude(JsonInclude.Include.NON_NULL)을 사용한다.

일단 컨트롤러는 다음과 같이 변경한다.

```Kotlin
    /**
     * 뮤지션 리스트를 반환한다.
     * @return ResponseResult<List<MusicianDto>>
     */
    @GetMapping("/musicians")
    fun musicians(pagination: Pagination): ResponseResult<List<MusicianDto>> {
        var musiciansWithPage = musicianService.fetchMusicians(PageRequest.of(pagination.offset, pagination.limit));
        with(musiciansWithPage) {
            pagination.totalCount = totalElements
            pagination.totalPage = totalPages
            pagination.last = isLast
        }
        return ResponseResult.of(musiciansWithPage.content, pagination)
    }
```

그리고 전체 뮤지션 정보를 가져오는 API를 여러 상황에 맞게 날려보자.

```json
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
        },
        {
            "id": 3,
            "name": "Charles Mingus",
            "genre": "Jazz"
        }
    ],
    "pagination": {
        "last": false,
        "totalPage": 2,
        "totalCount": 4,
        "currentPage": 1,
        "offset": 0,
        "limit": 3
    }
}

```

# At a Glance

여기서 사용하는 VO나 여러 방법이 항상 정답은 아닐것이다.

회사 또는 개발자의 습관이나 스타일에 따라 얼마든지 바뀔 수 있기 때문이다.

그럼에도 기본 컨셉은 그대로 유지될 것이기 때문이다.

이 브랜치에서는 delete관련 로직은 제외한다.       

스프링 부트에서 제공하는 delete를 통해 지우기 원하는 정보를 조회후 삭제하면 되기 때문에 save랑 똑같은 방식이기 때문이다.     

실제 DB로부터 지우는 방법도 있을 것이고 상태 컬럼을 통해 업데이트 하는 방식으로 진행될 것이기 때문이다.       





