package org.mikeneck.instant

import org.mikeneck.instant.EitherAssert.beRight
import run.ktcheck.Given
import run.ktcheck.KtCheck
import run.ktcheck.assertion.Matcher
import run.ktcheck.assertion.MatcherSupport
import run.ktcheck.assertion.NoDep.should

object EitherAssert {
  fun <L: Any, R: Any> beRight(): Matcher<Either<L, R>> =
      object : MatcherSupport<Either<L, R>>() {
        override val expectedValue: Any = "Either.Right"

        override fun matches(actual: Either<L, R>): Boolean = actual is Right
      }

  fun <L: Any, R: Any> beLeft(): Matcher<Either<L, R>> =
      object : MatcherSupport<Either<L, R>>() {
        override val expectedValue: Any = "Either.Left"

        override fun matches(actual: Either<L, R>): Boolean = actual is Left
      }

}

@Suppress("RemoveExplicitTypeArguments")
object AppFormatterUnixTest: KtCheck
by Given("Create App with 'instant -f unix'", { App.ofUnix() })
    .When("call 'formatter()'", { app -> app.formatter() })
    .Then("it should be Right", { _, either: Either<String, Formatter> -> 
      either should beRight<String, Formatter>() })


