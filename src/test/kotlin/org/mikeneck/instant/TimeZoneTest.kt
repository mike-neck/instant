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

import org.mikeneck.instant.EitherAssert.beLeft
import org.mikeneck.instant.EitherAssert.beRight
import run.ktcheck.Given
import run.ktcheck.KtCheck
import run.ktcheck.assertion.Matcher
import run.ktcheck.assertion.MatcherSupport
import run.ktcheck.assertion.NoDep.should
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.reflect.KClass

inline fun <reified T: Any> instanceOf(klass: KClass<T> = T::class): Matcher<T> =
    object : MatcherSupport<T>() {
      override val expectedValue: Any = "instance of ${klass.qualifiedName}"

      override fun matches(actual: T): Boolean =
          klass.isInstance(actual)
    }

inline fun <T: Any, reified R: T> beRightWithInstanceOf(klass: KClass<R> = R::class): Matcher<Either<String, T>> =
    object : MatcherSupport<Either<String, T>>() {
      override val expectedValue: Any get() = "Right(instance of ${klass.qualifiedName})"

      override fun matches(actual: Either<String, T>): Boolean =
          actual.map { klass.isInstance(it) }.rescue { false }
    }

object ParseToUtcTest: KtCheck
by Given("input is utc", { "utc" })
    .When("parse it", { TimeZone.parse(it) })
    .Then("it is UtcTimeZone", { _, either: Either<String, TimeZone> ->
      either should beRight<TimeZone>(UtcTimeZone)
    })

object ZoneOptionEmpty: KtCheck
by Given("input is empty", { "" })
    .When("parse it", { TimeZone.parse(it) })
    .Then("it is UtcTimeZone", { _, either: Either<String, TimeZone> ->
      either should beRight<TimeZone>(UtcTimeZone)
    })

object ZoneOptionIsPositiveInt: KtCheck
by Given("input is positive int(8)", { "8" })
    .When("parse it", { TimeZone.parse(it) })
    .Then("it is ZoneOffsetTimeZone", { _, either ->
      either should beRightWithInstanceOf<TimeZone, ZoneOffsetTimeZone>()
    })

object ZoneOptionsIsNegativeInt: KtCheck
by Given("input is negative int(-8)", { "-8" })
    .When("parse it", { TimeZone.parse(it) })
    .Then("it is ZoneOffsetTimeZone", { _, either ->
      either should beRightWithInstanceOf<TimeZone, ZoneOffsetTimeZone>()
    })

object ZoneOptionValidZoneId: KtCheck
by Given("input is PST8PDT", { "PST8PDT" })
    .When("parse it", { TimeZone.parse(it) })
    .Then("it is ZoneIdTimeZone", { _, either ->
      either should beRightWithInstanceOf<TimeZone, ZoneIdTimeZone>()
    })

@Suppress("RemoveExplicitTypeArguments")
object ZoneOptionInvalid: KtCheck
by Given("input is foo-bar-baz", { "foo-bar-baz" })
    .When("parse it", { TimeZone.parse(it) })
    .Then("it is Left", { _, either ->
      either should beLeft<String, TimeZone>()
    })

object UtcTimeZoneTest: KtCheck
by Given(
    description = "UtcTimeZone with time(2006-01-02T15:04:05Z)",
    before = { OffsetDateTime.of(
        LocalDate.of(2006, 1, 2),
        LocalTime.of(15, 4, 5),
        ZoneOffset.UTC
    ) },
    action = { UtcTimeZone }
)
    .When("apply timeZone to time", { it(this) })
    .Then("the same time should be returned", { _, resultTime ->
      resultTime should beRight(this)
    })

object ZoneOffsetTimeZoneTest: KtCheck
by Given(
    description = "ZoneOffsetTimeZone(9) with time(2006-01-02T15:04:05Z)",
    before = { OffsetDateTime.of(
        LocalDate.of(2006, 1, 2),
        LocalTime.of(15, 4, 5),
        ZoneOffset.UTC
    ) },
    action = { ZoneOffsetTimeZone(ZoneOffset.ofHours(9)) }
)
    .When("apply timeZone to time", { it(this) })
    .Then("it should be Right(2006-01-03T00:04:05+09:00)", { _, resultTime ->
      resultTime should 
          beRight(OffsetDateTime.of(
              LocalDate.of(2006, 1, 3),
              LocalTime.of(0, 4, 5),
              ZoneOffset.ofHours(9)
          ))
    })

object ZoneIdTimeZoneTest: KtCheck
by Given(
    description = "ZoneIdTimeZone(PST8PDT) with time(2006-01-02T15:04:05Z)",
    before = { OffsetDateTime.of(
        LocalDate.of(2006, 1, 2),
        LocalTime.of(15, 4, 5),
        ZoneOffset.UTC
    ) },
    action = { ZoneIdTimeZone(ZoneId.of("PST8PDT")) }
)
    .When("apply timeZone to time", { it(this) })
    .Then("it should be Right(2006-01-02T07:04:05-08:00)", { _, resultTime ->
      resultTime should 
          beRight(OffsetDateTime.of(
              LocalDate.of(2006, 1, 2),
              LocalTime.of(7, 4, 5),
              ZoneOffset.ofHours(-8)
          ))
    })
