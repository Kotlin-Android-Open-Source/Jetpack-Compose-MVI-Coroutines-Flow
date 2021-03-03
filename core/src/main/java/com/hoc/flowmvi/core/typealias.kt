package com.hoc.flowmvi.core

typealias Mapper<T, R> = (T) -> R

typealias IntentDispatcher<I> = (I) -> Unit
