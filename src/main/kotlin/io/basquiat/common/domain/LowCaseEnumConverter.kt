package io.basquiat.common.domain

import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * interfaceGenericEnum을 위한 추상 클래스
 * created by basquiat
 */
@Converter
abstract class LowCaseEnumConverter<T: Enum<T>> protected constructor(private val clazz: Class<T>) :
    AttributeConverter<T, String> where T: InterfaceGenericEnum<out T> {

    /**
     * enum 상수 값을 가져와서 lowerCase로 반환한다.
     * @param attribute
     * @return String
     */
    override fun convertToDatabaseColumn(attribute: T): String {
        var attribute: T? = attribute
        if (attribute == null) {
            val enums = clazz.enumConstants
            attribute = enums.first{ en -> en == en.defaultIfNull() }
                ?: throw IllegalArgumentException("해당 컬럼의 db enum값과 해당 enum클래스의 정보가 맞지 않습니다. 확인하세요.")
        }
        return attribute.name.lowercase()
    }

    /**
     * 디비 정보는 lowerCase로 upperCase로 변환후 비교후 해당 enum객체를 반환하게 한다.
     * @param dbData
     * @return T
     */
    override fun convertToEntityAttribute(dbData: String): T {
        val enums = clazz.enumConstants
        return try {
            enums.first{ en -> en.name == dbData.uppercase() }
                ?: throw IllegalArgumentException("해당 컬럼의 db enum값과 해당 enum클래스의 정보가 맞지 않습니다. 확인하세요.")
        } catch (e: NullPointerException) {
            defaultEnum(enums)
        }
    }

    /**
     * 반복되는 공통 코드 줄이자
     * @param enums
     * @return T
     */
    private fun defaultEnum(enums: Array<T>): T {
        return enums.first{ en -> en == en.defaultIfNull() }
    }
}