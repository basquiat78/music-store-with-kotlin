package io.basquiat.musicstore.album.repository

import io.basquiat.common.repository.BaseRepository
import io.basquiat.musicstore.album.domain.entity.Album
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query

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