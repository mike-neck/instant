package org.mikeneck.instant

import picocli.CommandLine
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.*

@CommandLine.Command(name = "checksum", mixinStandardHelpOptions = true, version = ["0.0"])
class App: Callable<Int> {

    @CommandLine.Option(names = ["-a", "--algorithm"], description = ["MD5 SHA-1, SHA-256"])
    var algorithm: String = "MD5"

    @CommandLine.Parameters(index = "0", description = ["target file"])
    lateinit var file: File

    override fun call(): Int =
        file.toPath()
            .let { Files.readAllBytes(it) }
            .let { it to MessageDigest.getInstance(algorithm) }
            .let { it.second.digest(it.first) }
            .let { "%0${it.size * 2}x" to BigInteger(1, it) }
            .let { it.first.format(it.second) }
            .let { println(it) }
            .let { 0 }
}

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
fun main(args: Array<String>) =
    System.exit(CommandLine(App()).execute(*args))
