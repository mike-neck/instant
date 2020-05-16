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
