package org.mikeneck.instant

import java.time.OffsetDateTime
import java.time.Duration

interface Adjustment: (OffsetDateTime) -> Either<String, OffsetDateTime> {
  companion object {
    fun parse(addOption: String): Either<String, Adjustment> =
        if (addOption.isEmpty()) Either.right(DurationAdjustment(Duration.ZERO))
        else trying { Duration.parse(addOption) }
            .map<Adjustment> { DurationAdjustment(it) }
            .errorMap { exception -> "${exception.message}" }
  }
}

data class DurationAdjustment(val duration: Duration): Adjustment {

  override fun invoke(before: OffsetDateTime): Either<String, OffsetDateTime> =
      Either.right(before + duration)
}
