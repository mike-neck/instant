package org.mikeneck.instant

import picocli.CommandLine
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.*

typealias Formatter = (OffsetDateTime) -> String

@CommandLine.Command(
    name = "instant", 
    mixinStandardHelpOptions = true,
    resourceBundle = "AppVersion",
    version = [
    "instant version \${bundle:app.version:-SNAPSHOT}",
    "Build OS: \${os.name}, \${os.version}, \${os.arch}",
    "With JVM: \${java.version}(\${java.vendor} \${java.vm.name} \${java.vm.version})",
    "Powered by Picocli ${CommandLine.VERSION}"
    ])
class App(private val clock: Clock = Clock.systemUTC()) : Callable<Int> {

  companion object {
    internal fun ofUnix(): App = App().apply { format = "unix" }

    internal fun withFormat(format: String): App = App().apply { this.format = format }

    internal fun App.withDuration(duration: String): App = this.apply { this.duration = duration }
  }

  @CommandLine.Option(
      names = ["-f", "--format"],
      description = [
        "Specifies output format. Available values are 'unix' or DateTimeFormatter pattern. default: \${DEFAULT-VALUE}"])
  var format: String = "uuuu-MM-dd'T'hh:mm:ss.nX"

  @Suppress("RemoveExplicitTypeArguments")
  internal fun formatter(): Either<String, Formatter> =
      when (format.toLowerCase()) {
        "unix" -> Either.right { dateTime: OffsetDateTime -> "${dateTime.toInstant().toEpochMilli()}" }
        else -> runCatching {
          DateTimeFormatter.ofPattern(format)
        }.fold(
            onSuccess = { formatter ->
              Either.right<String, Formatter> { dateTime: OffsetDateTime -> dateTime.format(formatter) }
            },
            onFailure = { Either.left("${it.message}") }
        )
      }

  @CommandLine.Option(
      names = ["-a", "--add", "--add-duration"],
      description = [
        "Add duration to instant. The format is 'PnDTnHnMn.nS'.",
        "The 'P' is fixed char.",
        "The 'nD' means days.",
        "The 'T' is fixed char required if inputting times.",
        "The 'nH' means hours.",
        "The 'nM' means minutes.",
        "The 'n.nS' means seconds and fractional seconds. The fractional seconds is optional.",
        "default: ''(empty)"
      ]
  )
  var duration: String = ""

  internal fun duration(): Either<String, Duration> =
      if (duration.isEmpty()) Either.right(Duration.ZERO)
      else runCatching { Duration.parse(duration) }
          .fold(
              onSuccess = { Either.right<String, Duration>(it) },
              onFailure = { Either.left("${it.message}: $duration") }
          )

  internal fun now() = OffsetDateTime.now(clock)

  internal fun showTime(formatter: Formatter): Either<String, String> =
      runCatching {
        formatter(now())
      }.fold(
          onSuccess = { Either.right(it) },
          onFailure = { Either.left("${it.message}") }
      )

  internal fun runProcess(): Pair<Int, String> =
      formatter()
          .flatMap { showTime(it) }
          .map { 0 to it }
          .rescue { message -> 1 to message }

  override fun call(): Int =
      runProcess().apply { println(this.second) }.first
}

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
fun main(args: Array<String>) =
    System.exit(CommandLine(App()).execute(*args))
