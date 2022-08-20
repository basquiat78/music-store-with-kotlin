package io.basquiat.musicstore.album.controller

import io.basquiat.common.domain.Pagination
import io.basquiat.common.domain.ResponseResult
import io.basquiat.common.util.setPagination
import io.basquiat.musicstore.album.domain.dto.AlbumDto
import io.basquiat.musicstore.album.service.AlbumService
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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