package io.basquiat.musicstore.album.service

import io.basquiat.common.util.toJson
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
        val albums = albumService.fetchMusicians(PageRequest.of(0, 10));
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

}