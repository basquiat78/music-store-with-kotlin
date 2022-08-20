package io.basquiat.musicstore.musician.repository

import io.basquiat.common.repository.BaseRepository
import io.basquiat.musicstore.musician.domain.code.GenreCode
import io.basquiat.musicstore.musician.domain.entity.Musician
import java.util.*

/**
 * MusicianRepository
 */
interface MusicianRepository : BaseRepository<Musician, Long> {

    fun findByName(name: String) : Musician?
    fun findByGenre(genre: GenreCode) : List<Musician>

}