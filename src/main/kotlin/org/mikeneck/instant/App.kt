package org.mikeneck.instant

import picocli.CommandLine
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.*

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

    internal fun <L: Any, R: Any, N: Any> Either<L, (R) -> Either<L, N>>.flatten(): (R) -> Either<L, N> =
        { right: R -> this.flatMap { mapping -> mapping(right) } }

    internal operator fun <L: Any, R: Any, M: Any, N: Any> Either<L, (R) -> Either<L, M>>.plus(next: Either<L, (M) -> Either<L, N>> ): Either<L, (R) -> Either<L, N>> =
        this.flatMap { function: (R) -> Either<L, M> -> 
          next.map { nextFunction: (M) -> Either<L, N> ->
            { right: R -> function(right).flatMap(nextFunction) }
          }
        }
  }

  @CommandLine.Option(
      names = ["-f", "--format"],
      description = [
        "Specifies output format. Available values are 'unix' or DateTimeFormatter pattern. default: \${DEFAULT-VALUE}"])
  var format: String = "uuuu-MM-dd'T'hh:mm:ss.nX"

  @Suppress("RemoveExplicitTypeArguments")
  internal fun formatter(): Either<String, Formatter> = Formatter.parse(format)

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

  @Suppress("RemoveExplicitTypeArguments")
  internal fun duration(): Either<String, Duration> =
      if (duration.isEmpty()) Either.right(Duration.ZERO)
      else runCatching { Duration.parse(duration) }
          .fold(
              onSuccess = { Either.right<String, Duration>(it) },
              onFailure = { Either.left("${it.message}: $duration") }
          )

  @Suppress("RemoveExplicitTypeArguments")
  private val Either<String, Duration>.mapper: Either<String, (OffsetDateTime) -> Either<String, OffsetDateTime>> get() =
    this.map { duration -> { offsetDateTime: OffsetDateTime -> 
      Either.right<String, OffsetDateTime>(offsetDateTime + duration) } }

  private val composedFunction: (OffsetDateTime) -> Either<String, String> get() =
      (duration().mapper + formatter()).flatten()

  internal fun now() = OffsetDateTime.now(clock)

  internal fun runProcess(): Pair<Int, String> =
      composedFunction(now())
          .map { 0 to it }
          .rescue { message -> 1 to message }

  override fun call(): Int =
      runProcess().apply { println(this.second) }.first
}

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
fun main(args: Array<String>) =
    System.exit(CommandLine(App()).execute(*args))
