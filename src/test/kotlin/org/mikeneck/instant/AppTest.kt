package org.mikeneck.instant

import org.mikeneck.instant.EitherAssert.beLeft
import org.mikeneck.instant.EitherAssert.beLeftContaining
import org.mikeneck.instant.EitherAssert.beRight
import run.ktcheck.Given
import run.ktcheck.KtCheck
import run.ktcheck.assertion.Matcher
import run.ktcheck.assertion.MatcherSupport
import run.ktcheck.assertion.NoDep.should
import run.ktcheck.assertion.NoDep.shouldBe
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object EitherAssert {
  fun <L : Any, R : Any> beRight(): Matcher<Either<L, R>> =
      object : MatcherSupport<Either<L, R>>() {
        override val expectedValue: Any = "Either.Right"

        override fun matches(actual: Either<L, R>): Boolean = actual is Right
      }

  fun beRight(value: String): Matcher<Either<String, String>> =
      object : MatcherSupport<Either<String, String>>() {
        override val expectedValue: Any get() = "Right with value[$value]"

        override fun matches(actual: Either<String, String>): Boolean =
            actual == Either.right<String, String>(value)
      }

  fun <L : Any, R : Any> beLeft(): Matcher<Either<L, R>> =
      object : MatcherSupport<Either<L, R>>() {
        override val expectedValue: Any = "Either.Left"

        override fun matches(actual: Either<L, R>): Boolean = actual is Left
      }

  fun beLeftContaining(pattern: String): Matcher<Either<String, String>> =
      object : MatcherSupport<Either<String, String>>() {
        override val expectedValue: Any = "Left with value containing $pattern" 

        override fun matches(actual: Either<String, String>): Boolean =
            when (actual) {
              is Right -> false
              is Left -> actual.value.contains(pattern)
            }
      }
}

@Suppress("RemoveExplicitTypeArguments")
object AppFormatterUnixTest : KtCheck
by Given("Create App with 'instant -f unix'", { App.ofUnix() })
    .When("call 'formatter()'", { app ->
      app.formatter()
    })
    .Then("it should be Right", { _, either ->
      either should beRight<String, Formatter>()
    })

@Suppress("RemoveExplicitTypeArguments")
object AppFormatterValidFormatTest : KtCheck
by Given("Create App with `instant -f 'uuuu/MM/dd hh:mm:ss.nX'`", { 
  App.withFormat("uuuu/MM/dd hh:mm:ss.nX") 
})
    .When("call `formatter()`", { app ->
      app.formatter()
    })
    .Then("it should be Right", { _, either ->
      either should beRight<String, Formatter>()
    })

object AppFormatterInvalidFormatTest : KtCheck
by Given("Create App with `instant -f 'foo-bar-baz'`", {
  App.withFormat("foo-bar-baz")
})
    .When("call `formatter()`", { app -> 
      app.formatter()
    }).Then("it should be Left", { _, either ->
      either should beLeft<String, Formatter>()
    })

object AppNowTest : KtCheck
by Given(
    description = "Create App with fixed Instant Clock",
    before = { OffsetDateTime.of(
        LocalDate.of(2020, 1, 2),
        LocalTime.of(15, 4), ZoneOffset.UTC) },
    action = { App(Clock.fixed(this.toInstant(), ZoneId.of("UTC"))) }
)
    .When("call now()", { app -> app.now() })
    .Then("it is the same offsetDateTime as the context", { _, offsetDateTime ->
      offsetDateTime shouldBe this
    })

object AppShowTimeTest : KtCheck
by Given(
    description = "Create App with fixed Instant Clock",
    before = { OffsetDateTime.of(
        LocalDate.of(2020, 1, 2),
        LocalTime.of(15, 4), ZoneOffset.UTC) },
    action = { App(Clock.fixed(this.toInstant(), ZoneId.of("UTC"))) } 
)
    .When("call showTime with unix like formatter", { app -> 
      app.showTime { "${it.toInstant().toEpochMilli()}" }
    })
    .Then("it should Right of integral string", { _, either ->
      either should beRight("${this.toInstant().toEpochMilli()}")
    })
    .When("call showTime with dateTimeFormatter", { app ->
      app.showTime { DateTimeFormatter.ISO_OFFSET_DATE.format(it) }
    })
    .Then("it should be Right of 2020-01-02Z", { _, either ->
      either should beRight("2020-01-02Z")
    })
    .When("call showTime with unavailable pattern", { app ->
      app.showTime { DateTimeFormatter.ofPattern("z").format(it) }
    })
    .Then("it should be Left with message containing 'Unable to extract ZoneId'", { _, either ->
      either should beLeftContaining("Unable to extract ZoneId")
    })
