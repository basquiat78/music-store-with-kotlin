package io.basquiat.musicstore.musician.controller

import io.basquiat.common.domain.ResponseResult
import io.basquiat.musicstore.musician.domain.dto.MusicianDto
import io.basquiat.musicstore.musician.service.MusicianService
import org.springframework.web.bind.annotation.GetMapping
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

    @GetMapping("/musicians")
    fun musicians() : ResponseResult<List<MusicianDto>> {
        return ResponseResult.of(musicianService.fetchMusicians())
    }

}