package org.mikeneck.instant

import run.ktcheck.assertion.Matcher
import run.ktcheck.assertion.MatcherSupport

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