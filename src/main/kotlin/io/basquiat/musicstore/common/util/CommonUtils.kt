package io.basquiat.musicstore.common.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.basquiat.musicstore.common.domain.Pagination
import io.basquiat.musicstore.common.exception.MandatoryArgumentException
import io.basquiat.musicstore.common.exception.ObjectEmptyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page

/**
 * 메세지가 없는 경우
 */
fun objectEmpty(): Nothing {
    throw ObjectEmptyException()
}

/**
 * 메세지가 있는 경우
 */
fun objectEmpty(message: String?): Nothing {
    if(message == null) {
        objectEmpty()
    } else {
        throw ObjectEmptyException(message)
    }
}

/**
 * 메세지가 없는 경우
 */
fun mandatoryParam(): Nothing {
    throw MandatoryArgumentException()
}

/**
 * 메세지가 있는 경우
 */
fun mandatoryParam(message: String?): Nothing {
    if(message == null) {
        mandatoryParam()
    } else {
        throw MandatoryArgumentException(message)
    }
}

/**
 * kotlin jackson object mapper
 */
val mapper = jacksonObjectMapper()

/**
 * 객체를 받아서 json 스트링으로 반환한다.
 */
fun toJson(any: Any): String = mapper.writeValueAsString(any)

/**
 * json 스트링을 해당 객체로 매핑해서 반환한다.
 */
fun <T> convertToObject(json: String, valueType: Class<T>): T = mapper.readValue(json, valueType)

/**
 * json 스트링이 리스트 형식인 경우 해당 객체로 매핑해서 리스트 형식으로 반환한다.
 */
fun <T> convertToObject(json: String, valueType: TypeReference<T>): T = mapper.readValue(json, valueType)

/**
 * TypeReference를 편하게 쓰기 위한 메소드
 */
inline fun <reified T> typeRef(): TypeReference<List<T>> = object: TypeReference<List<T>>() {}

/**
 * pagination정보를 만드는 메소드
 */
fun <T> setPagination(pagination: Pagination, page: Page<T>) {
    with(page) {
        pagination.totalCount = totalElements
        pagination.totalPage = totalPages
        pagination.last = isLast
    }
}

/**
 * 클래스별로 로거 설정할 수 있도록 Inline, reified를 통해 제너릭하게 사용해 보자.
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)