package org.mikeneck.instant

import org.mikeneck.instant.App.Companion.withDuration
import org.mikeneck.instant.EitherAssert.beLeft
import org.mikeneck.instant.EitherAssert.beLeftContaining
import org.mikeneck.instant.EitherAssert.beRight
import picocli.CommandLine
import run.ktcheck.Given
import run.ktcheck.KtCheck
import run.ktcheck.assertion.NoDep.should
import run.ktcheck.assertion.NoDep.shouldBe
import run.ktcheck.assertion.NoDep.shouldNotBe
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

@Suppress("RemoveExplicitTypeArguments")
object AppFormatterInvalidFormatTest : KtCheck
by Given("Create App with `instant -f 'foo-bar-baz'`", {
  App.withFormat("foo-bar-baz")
})
    .When("call `formatter()`", { app -> 
      app.formatter()
    }).Then("it should be Left", { _, either ->
      either should beLeft<String, Formatter>()
    })

@Suppress("RemoveExplicitTypeArguments")
object DurationInvalidFormatTest: KtCheck
by Given(
    "invalid format('P-1H30M') is given",
    { App().withDuration("P-1H30M") })
    .When("call `duration()`", { app ->
      app.duration()
    }).Then("it should be Left", { _, either ->
      either should beLeft<String, Adjustment>()
    })

object DurationWithoutInputTest: KtCheck
by Given("without --add option", { App() })
    .When("call `duration()`", { app ->
      app.duration()
    })
    .Then("it should be Right with zero duration", {  _, either ->
      either should beRight<Adjustment, Adjustment>(DurationAdjustment(Duration.ZERO))
    })

object DurationWithValidFormatTest: KtCheck
by Given("valid format duration(P1DT-12H)", { App().withDuration("P1DT-12H") })
    .When("call `duration()`", { app -> app.duration() })
    .Then("it should be Right with 12 hours", { _, either ->
      either should beRight<Adjustment, Adjustment>(DurationAdjustment(Duration.ofHours(12)))
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

private fun formatter(f: (OffsetDateTime) -> String): Formatter = object : Formatter {
  override fun invoke(offsetDateTime: OffsetDateTime): Either<String, String> =
      runCatching { f(offsetDateTime) }.fold(
          onSuccess = { Either.right(it) },
          onFailure = { Either.left("${it.message}") }
      )
}

object AppTest : KtCheck
by Given(
    description = "Create CommandLine with fixed Instant Clock",
    before = { OffsetDateTime.of(
        LocalDate.of(2020, 1, 2),
        LocalTime.of(15, 4), ZoneOffset.UTC) },
    action = { CommandLine(App(Clock.fixed(this.toInstant(), ZoneId.of("UTC")))) }
)
    .When("run it without param", { commandLine -> 
      commandLine.execute()
    })
    .Then("results 0", { _, exit ->
      exit shouldBe 0
    })

    .When("run it without unknown param", { commandLine -> 
      commandLine.execute("--unknown-option=foo")
    })
    .Then("results 2", { _, exit ->
      exit shouldBe 2
    })

    .When("run it with invalid param", { commandLine -> 
      commandLine.execute("--format=<unrecognized-format>")
    })
    .Then("results 1", { _, exit ->
      exit shouldBe 1
    })

    .When("run it with format unix", { commandLine -> 
      commandLine.execute("-f", "unix")
    })
    .Then("results 0", { _, exit ->
      exit shouldBe 0
    })
