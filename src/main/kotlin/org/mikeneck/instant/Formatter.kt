package org.mikeneck.instant

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface Formatter: (OffsetDateTime) -> Either<String, String> {
  companion object {
    fun parse(formatterOption: String): Either<String, Formatter> =
        when (formatterOption.toLowerCase()) {
          "unix" -> Either.right(UnixTimeFormatter)
          else -> ParsedDateTimeFormatter.parse(formatterOption)
        }
  }
}

object UnixTimeFormatter: Formatter {

  override fun invoke(offsetDateTime: OffsetDateTime): Either<String, String> =
      Either.right("${offsetDateTime.toInstant().toEpochMilli()}")
}

data class ParsedDateTimeFormatter(val dateTimeFormatter: DateTimeFormatter): Formatter {

  override fun invoke(offsetDateTime: OffsetDateTime): Either<String, String> =
      trying { offsetDateTime.format(dateTimeFormatter) }.errorMap { "${it.message}" }

  companion object {
    fun parse(formatterOption: String): Either<String, Formatter> =
        trying { DateTimeFormatter.ofPattern(formatterOption) }
            .map<Formatter> { ParsedDateTimeFormatter(it) }
            .errorMap { "${it.message}" }
  }
}
