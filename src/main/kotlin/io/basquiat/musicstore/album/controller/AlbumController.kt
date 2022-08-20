package io.basquiat.musicstore.album.controller

import io.basquiat.musicstore.common.domain.Pagination
import io.basquiat.musicstore.common.domain.ResponseResult
import io.basquiat.musicstore.common.util.logger
import io.basquiat.musicstore.common.util.setPagination
import io.basquiat.musicstore.album.domain.dto.AlbumDto
import io.basquiat.musicstore.album.service.AlbumService
import io.basquiat.musicstore.musician.domain.vo.AlbumRequest
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

/**
 * 앨범 정보와 관련된 컨트롤러
 * created by basquiat
 */
@RestController
@RequestMapping("/api/music/store")
class AlbumController(
    private val albumService: AlbumService,
) {

    val log = logger<AlbumController>()

    /**
     * 앨범 리스트를 반환한다.
     * @return ResponseResult<List<AlbumDto>>
     */
    @GetMapping("/albums")
    fun albums(pagination: Pagination): ResponseResult<List<AlbumDto>> {
        log.info("로깅 테스트")
        val albumsWithPage = albumService.fetchAlbums(PageRequest.of(pagination.offset, pagination.limit))
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

    /**
     * 앨범 정보를 생성한다.
     * @param request: AlbumRequest
     * @return ResponseResult<AlbumDto>
     */
    @PostMapping("/albums")
    fun createAlbum(@RequestBody request: AlbumRequest) = ResponseResult.of(albumService.createAlbum(request))

    /**
     * 뮤지션 정보를 변경하고 변경된 정보를 담아서 보내준다.
     * @param request: AlbumRequest
     * @return ResponseResult<AlbumDto>
     */
    @PatchMapping("/albums")
    fun updateAlum(@RequestBody request: AlbumRequest) = ResponseResult.of(albumService.updateAlbum(request))

}