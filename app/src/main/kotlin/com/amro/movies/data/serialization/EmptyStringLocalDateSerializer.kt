package com.amro.movies.data.serialization

import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for [LocalDate] that handles empty strings by treating them as null.
 *
 * This is useful for APIs that might return an empty string for a date field instead
 * of a null value or a valid ISO-8601 date.
 */
object EmptyStringLocalDateSerializer : KSerializer<LocalDate?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate? {
        val string = decoder.decodeString()
        return if (string.isBlank()) null else LocalDate.parse(string)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: LocalDate?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.toString())
        }
    }
}
