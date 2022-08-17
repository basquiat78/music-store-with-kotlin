package io.basquiat.common.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 * Rest API response 정보를 담은 객체
 */
@JsonPropertyOrder("result", "pagination")
data class ResponseResult<T>(
    private val _result: T?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val pagination: Pagination?
) {

    val result get() = this._result

    companion object {
        /**
         * ResponseResult를 생성하는 정적 메소드
         * @param result
         * @return ResponseResult<T>
         */
        fun <T> of(result: T?, pagination: Pagination? = null) = ResponseResult(result, pagination)
    }

}