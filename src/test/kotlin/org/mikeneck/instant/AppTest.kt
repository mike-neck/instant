/*
 * Copyright 2020 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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


