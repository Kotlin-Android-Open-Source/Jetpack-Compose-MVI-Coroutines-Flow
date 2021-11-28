package com.hoc.flowmvi.core

interface Mapper<T, R> : (T) -> R {
  override operator fun invoke(param: T): R
}
