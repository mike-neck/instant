package org.mikeneck.instant

import org.mikeneck.instant.EitherAssert.beLeft
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

object EitherAssert {
  fun <L : Any, R : Any> beRight(): Matcher<Either<L, R>> =
      object : MatcherSupport<Either<L, R>>() {
        override val expectedValue: Any = "Either.Right"

        override fun matches(actual: Either<L, R>): Boolean = actual is Right
      }

  fun <L : Any, R : Any> beLeft(): Matcher<Either<L, R>> =
      object : MatcherSupport<Either<L, R>>() {
        override val expectedValue: Any = "Either.Left"

        override fun matches(actual: Either<L, R>): Boolean = actual is Left
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
