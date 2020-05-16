/*
 * Copyright 2020 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
