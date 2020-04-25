package org.mikeneck.instant

import picocli.CommandLine
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.*

typealias Formatter = (OffsetDateTime) -> String

@CommandLine.Command(name = "instant", mixinStandardHelpOptions = true, version = ["0"])
class App(private val clock: Clock = Clock.systemUTC()) : Callable<Int> {

  @CommandLine.Option(
      names = ["-f", "--format"],
      description = [
        "Specifies output format. available values are 'unix' or DateTimeFormatter pattern. default: \${DEFAULT-VALUE}"])
  var format: String = "uuuu-MM-dd'T'hh:mm:ss.nX"

  fun formatter(): Either<String, Formatter> =
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
