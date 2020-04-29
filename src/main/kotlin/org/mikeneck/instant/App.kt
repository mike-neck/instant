package org.mikeneck.instant

import picocli.CommandLine
import java.time.Clock
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
  }

  @CommandLine.Option(
      names = ["-f", "--format"],
      description = [
        "Specifies output format. available values are 'unix' or DateTimeFormatter pattern. default: \${DEFAULT-VALUE}"])
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
