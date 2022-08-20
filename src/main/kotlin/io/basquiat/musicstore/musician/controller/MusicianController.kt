package io.basquiat.musicstore.musician.controller

import io.basquiat.common.domain.Pagination
import io.basquiat.common.domain.ResponseResult
import io.basquiat.common.util.mandatoryParam
import io.basquiat.common.util.setPagination
import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.dto.MusicianDto
import io.basquiat.musicstore.musician.domain.vo.MusicianRequest
import io.basquiat.musicstore.musician.service.MusicianService
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

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
    fun musicians(pagination: Pagination): ResponseResult<List<MusicianDto>> {
        val musiciansWithPage = musicianService.fetchMusicians(PageRequest.of(pagination.offset, pagination.limit));
        setPagination(pagination, musiciansWithPage)
        return ResponseResult.of(musiciansWithPage.content, pagination)
    }

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

    /**
     * 뮤지션 정보를 생성한다.
     * @param request: MusicianRequest
     *
     */
    @PostMapping("/musicians")
    fun createMusician(@RequestBody request: MusicianRequest) {
        musicianService.createMusician(request)
    }

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

}