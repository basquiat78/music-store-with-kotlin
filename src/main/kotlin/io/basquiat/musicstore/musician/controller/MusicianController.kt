package io.basquiat.musicstore.musician.controller

import io.basquiat.common.domain.ResponseResult
import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.dto.MusicianDto
import io.basquiat.musicstore.musician.service.MusicianService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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