package org.evoleq.math.cat.suspend.lens

import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.comonad.store.IStore
import org.evoleq.math.cat.suspend.comonad.store.KeyStore
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

typealias KeyLens<S,T,A,B> = ScopedSuspended<S,KeyStore<A, B, T>>

@MathCatDsl
@Suppress("FunctionName")
fun <S,T,A,B> KeyLens(arrow: suspend CoroutineScope.(S)->KeyStore<A,B,T>): KeyLens<S,T,A,B> = ScopedSuspended(arrow)

@MathCatDsl
fun <S,T,A,B> KeyLens<S, T, A, B>.getter(): ScopedSuspended<S,A> = ScopedSuspended { s -> by(this@getter)(s).data }

@MathCatDsl
fun <S,T,A,B> KeyLens<S, T, A, B>.setter(): ScopedSuspended<S,ScopedSuspended<B, T>> = ScopedSuspended { s ->
     with(by(this@setter)(s)) {
         ScopedSuspended { b -> by(by(this@with)(b))(this@with.data) }
     }
}

suspend operator fun <S, T, A, B, C, D> KeyLens<S, T, A, B>.times(other: KeyLens<A, B, C, D>): KeyLens<S, T, C, D> = KeyLens{s->
    val storeA = by(this@times)(s)
    val a = storeA.data
    val storeC = by(other)(a)
    val c = storeC.data
    
    val dToCToB: suspend CoroutineScope.(D) -> ScopedSuspended<C,B> = by(storeC)
    val bToAToT: suspend CoroutineScope.(B) -> ScopedSuspended<A,T> = by(storeA)
    
    KeyStore(c) {
        d -> ScopedSuspended{c -> by(bToAToT(by(dToCToB(d))(c)))(a) }
    }
}