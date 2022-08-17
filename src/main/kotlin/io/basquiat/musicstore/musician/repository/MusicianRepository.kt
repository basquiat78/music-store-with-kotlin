package io.basquiat.musicstore.musician.repository

import io.basquiat.common.repository.BaseRepository
import io.basquiat.musicstore.musician.domain.entity.Musician

/**
 * MusicianRepository
 */
interface MusicianRepository : BaseRepository<Musician, Long>