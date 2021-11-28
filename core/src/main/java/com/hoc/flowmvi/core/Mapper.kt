package com.hoc.flowmvi.core

interface Mapper<T, R> {
  operator fun invoke(param: T): R
}
