package org.evoleq.math.cat.suspend.lens

import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.comonad.store.KeyStore
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

typealias KeyLens<V,W,K,P> = ScopedSuspended<V,KeyStore<W,K,P>>

@MathCatDsl
@Suppress("FunctionName")
fun <V,W,K,P> KeyLens(arrow: suspend CoroutineScope.(V)->KeyStore<W,K,P>): KeyLens<V,W,K,P> = ScopedSuspended(arrow)

suspend fun <U,V,W,K,P,L,Q> KeyLens<U,V,K,P>.times(other: KeyLens<P,W,L,Q>): KeyLens<U,V,Pair<K,L>,Q> = KeyLens{
    u -> with(by(this@times)(u)) kS@{KeyStore<V,Pair<K,L>,Q>(this@kS.data) {
        pair -> ScopedSuspended { v ->
            val p = by(by(this@kS)(pair.first)) (v)
            val x = by(other)(p)
            val y = by(x)(pair.second)
            TODO()
        }
    }}
}


/*
suspend fun <U,V,W,K,P,Q>  KeyLens<U, V, K, P>.times(other: KeyLens<V,W,P,Q>): KeyLens<U,V,P,Q> = KeyLens {
    u ->
        val ks1 = by(this@times)(u)
        val data1 = ks1.data
        val arrow1 = by(ks1)
    
        val ks2 = by(other)(data1)
        val data2 = ks2.data
        val arrow2 = by(ks2)
    
        val store: KeyStore<W, K, Q> = KeyStore(data2) {
            k -> ScopedSuspended { w -> by(arrow2(by(arrow1(k))(data1)))(w) }
        }
        val store1: KeyStore<V,P,Q> = KeyStore(data1) {
            p -> ScopedSuspended {
                v ->
            }
        }
        store1
}

 */