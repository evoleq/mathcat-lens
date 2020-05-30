package org.evoleq.math.cat.suspend.lens

import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.comonad.store.IStore
import org.evoleq.math.cat.suspend.comonad.store.toStore
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

typealias ILens<S,T,A,B> = ScopedSuspended<S,IStore<A,B,T>>

@Suppress("FunctionName")
@MathCatDsl
fun <S,T,A,B> ILens(arrow: suspend CoroutineScope.(S)->IStore<A,B,T>): ILens<S,T,A,B> = ScopedSuspended(arrow)

@MathCatDsl
fun <S, A> ILens<S,S,A,A>.toLens(): Lens<S, A> = Lens {
    w -> by(this@toLens)(w).toStore()
}

suspend fun <R, S, T, A, B, C> ILens<R,S,B,C>.times(other: ILens<S, T,A, B>): ILens<R,T,A,C> = ILens{
    r -> val storeB = by(this@times)(r)
        val lambda1 = by(this@times)
        val stroreBPrime = storeB.map(by(other))
    TODO()
}

