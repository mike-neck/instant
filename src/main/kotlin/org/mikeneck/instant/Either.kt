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

/**
 * Represents Success(Right) or Failure(Left)
 */
sealed class Either<L: Any, R: Any> {

  /**
   * Maps a current right value to a next value, using given mapping function.
   * If this is failure, then this will keep a left value.
   */
  abstract fun <T: Any> map(mapping: (R) -> T): Either<L, T>

  /**
   * Maps a current right value to a next value, using given mapping function.
   * If this is failure, then this will keep a left value.
   */
  abstract fun <T: Any> flatMap(mapping: (R) -> Either<L, T>): Either<L, T>

  /**
   * Maps a current left value to a next value, using given mapping function.
   * If this is right, then this will keep a right value.
   */
  abstract fun <T: Any> errorMap(mapping: (L) -> T): Either<T, R>

  /**
   * Converts a current left value to a right type, to get the result in the same type.
   * If this is right, then this will return the current value.
   */
  abstract fun rescue(mapping: (L) -> R): R

  companion object {
    @JvmStatic
    fun <L: Any, R: Any> right(value: R): Either<L, R> = Right(value)

    @JvmStatic
    fun <L: Any, R: Any> left(value: L): Either<L, R> = Left(value)
  }
}

internal data class Right<L: Any, R: Any>(val value: R): Either<L, R>() {
  override fun <T : Any> map(mapping: (R) -> T): Either<L, T> = Right(mapping(value))

  override fun <T : Any> flatMap(mapping: (R) -> Either<L, T>): Either<L, T> = mapping(value)

  override fun <T : Any> errorMap(mapping: (L) -> T): Either<T, R> = Right(value)

  override fun rescue(mapping: (L) -> R): R = value
}

internal data class Left<L: Any, R: Any>(val value: L): Either<L, R>() {
  override fun <T : Any> map(mapping: (R) -> T): Either<L, T> = Left(value)

  override fun <T : Any> flatMap(mapping: (R) -> Either<L, T>): Either<L, T> = Left(value)

  override fun <T : Any> errorMap(mapping: (L) -> T): Either<T, R> = Left(mapping(value))

  override fun rescue(mapping: (L) -> R): R = mapping(value)
}
