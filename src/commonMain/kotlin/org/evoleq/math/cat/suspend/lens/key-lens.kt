/**
 * Copyright (c) 2020 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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