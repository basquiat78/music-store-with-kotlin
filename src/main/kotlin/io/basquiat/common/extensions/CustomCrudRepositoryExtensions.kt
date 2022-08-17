package io.basquiat.common.extensions

import io.basquiat.common.util.objectEmpty
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

/**
 * null을 반환하지 않고 OptionalEmptyException에러를 날리자.
 */
fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID, message: String? = null): T = this.findByIdOrNull(id) ?: objectEmpty(message)