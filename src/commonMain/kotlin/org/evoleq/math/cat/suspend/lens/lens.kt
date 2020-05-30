package org.evoleq.math.cat.suspend.lens

import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.comonad.store.Store
import org.evoleq.math.cat.suspend.comonad.store.indexed
import org.evoleq.math.cat.suspend.comonad.store.keyStore
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

typealias Lens<W,P> = ScopedSuspended<W, Store<P, W>>

@MathCatDsl
@Suppress("FunctionName")
fun <W,P> Lens(arrow: suspend CoroutineScope.(W)->Store<P,W>): Lens<W, P> = ScopedSuspended(arrow)

@MathCatDsl
fun <W, P> Lens<W, P>.getter(): ScopedSuspended<W, P> = ScopedSuspended {
    w ->by(this@getter)(w).data
}
@MathCatDsl
fun <W, P> Lens<W, P>.setter(p:P): ScopedSuspended<W, W> = ScopedSuspended {
    w -> by(by(this@setter)(w))(p)
}
@MathCatDsl
fun <W, P> Lens<W, P>.setter(): ScopedSuspended<W, ScopedSuspended<P,W>> = ScopedSuspended(morphism)

@MathCatDsl
fun <W, P> Lens<W,P>.indexed(): ILens<W,W,P,P> = ILens{
    w -> (by(this@indexed)(w)).indexed()
}

fun <W, P> Lens<W, P>.keys(): KeyLens<W,W,P,P> = KeyLens {
    w -> (by(this@keys)(w)).keyStore()
}

suspend operator fun <W, P, Q> Lens<W,P>.times(other: Lens<P,Q>): Lens<W, Q> = (this.indexed() * other.indexed()).toLens()

suspend operator fun <W, P, Q> Lens<W,P>.div(other: Lens<Q,W>): Lens<Q, P> = other * this
//(this.indexed() / other.indexed()).toLens()
