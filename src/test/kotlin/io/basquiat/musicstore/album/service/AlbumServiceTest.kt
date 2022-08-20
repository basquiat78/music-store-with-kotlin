package io.basquiat.musicstore.album.service

import io.basquiat.musicstore.common.domain.YesOrNo
import io.basquiat.musicstore.common.util.toJson
import io.basquiat.musicstore.musician.domain.vo.AlbumRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class AlbumServiceTest @Autowired constructor(
    private val albumService: AlbumService,
) {

    @Test
    @Transactional
    @DisplayName("앨범 리스트를 가져온다.")
    fun fetchAlums_TEST() {
        val albums = albumService.fetchAlbums(PageRequest.of(0, 10));
        println(toJson(albums))
    }

    @Test
    @Transactional
    @DisplayName("특정 뮤지션의 앨범 리스트를 가져온다.")
    fun fetchAlumsByMusicianId_TEST() {
        val page = albumService.fetchAlbumsByMusicianId(1L, PageRequest.of(0, 10));
        println(toJson(page))
        val albums = page.content
        println(albums[0].title)
        println(albums[0].musicianName)
        println(albums[1].title)
        println(albums[1].musicianName)
    }

    @Test
    @Transactional
    @DisplayName("앨범명으로 앨범 리스트를 가져온다.")
    fun fetchAlbumsByTitle_TEST() {
        val page = albumService.fetchAlbumsByTitle("Now Is The Time", PageRequest.of(0, 10));
        val albums = page.content
        println(albums[0].title)
        println(albums[0].musicianName)
        println(albums[1].title)
        println(albums[1].musicianName)

    }

    @Test
    @DisplayName("앨범을 생성한다.")
    fun saveAlbum_TEST() {
        // John Coltrane의 음반 Ballads를 생성해보자.
        val request = AlbumRequest(musicianId = 2, title = "Ballads", rep = YesOrNo.Y)
        val createAlbum = albumService.createAlbum(request)
        with(createAlbum) {
            assertThat(musicianName).isEqualTo("John Coltrane")
            assertThat(title).isEqualTo("Ballads")
        }

    }

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


}