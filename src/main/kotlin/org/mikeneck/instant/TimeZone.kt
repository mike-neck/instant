package org.mikeneck.instant

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

interface TimeZone: (OffsetDateTime) -> Either<String, OffsetDateTime> {

  companion object {
    fun parse(zoneOption: String): Either<String, TimeZone> =
        when {
          zoneOption.isEmpty() || zoneOption.toUpperCase() == "UTC" -> Either.right(UtcTimeZone)
          else -> 
            ZoneOffsetTimeZone.parse(zoneOption)
                .rescue { ZoneIdTimeZone.parse(zoneOption) }
        }
  }
}

object UtcTimeZone: TimeZone {
  override fun invoke(offsetDateTime: OffsetDateTime): Either<String, OffsetDateTime> =
      Either.right(offsetDateTime)
}

data class ZoneOffsetTimeZone(val zoneOffset: ZoneOffset): TimeZone {

  override fun invoke(offsetDateTime: OffsetDateTime): Either<String, OffsetDateTime> =
      Either.right(offsetDateTime.toInstant().atOffset(zoneOffset))

  companion object {
    @Suppress("RemoveExplicitTypeArguments")
    fun parse(zoneOption: String): Either<Throwable, Either<String, TimeZone>> =
        trying { zoneOption.toInt() }
            .map { ZoneOffsetTimeZone(ZoneOffset.ofHours(it)) }
            .map { Either.right<String, TimeZone>(it) }
  }
}

data class ZoneIdTimeZone(val zoneId: ZoneId): TimeZone {

  override fun invoke(offsetDateTime: OffsetDateTime): Either<String, OffsetDateTime> =
      Either.right(offsetDateTime.toInstant().atZone(zoneId).toOffsetDateTime())

  companion object {
    fun parse(zoneOption: String): Either<String, TimeZone> =
        trying { ZoneId.of(zoneOption) }
            .map<TimeZone> { ZoneIdTimeZone(it) }
            .errorMap { "${it.message}" }
  }
}
