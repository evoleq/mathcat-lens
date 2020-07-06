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
package org.evoleq.math.cat.optic.lens

import org.evoleq.math.cat.comonad.store.KeyStore
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.morphism.Morphism
import org.evoleq.math.cat.morphism.by

typealias KeyLens<S,T,A,B> = Morphism<S, KeyStore<A, B, T>>

@MathCatDsl
@Suppress("FunctionName")
fun <S,T,A,B> KeyLens(arrow: (S)->KeyStore<A,B,T>): KeyLens<S, T, A, B> = Morphism(arrow)

@MathCatDsl
fun <S,T,A,B> KeyLens<S, T, A, B>.getter(): Morphism<S,A> = Morphism { s -> by(this@getter)(s).data }

@MathCatDsl
fun <S,T,A,B> KeyLens<S, T, A, B>.setter(): Morphism<S,Morphism<B, T>> = Morphism { s ->
     with(by(this@setter)(s)) {
         Morphism { b -> by(by(this@with)(b))(this@with.data) }
     }
}

operator fun <S, T, A, B, C, D> KeyLens<S, T, A, B>.times(other: KeyLens<A, B, C, D>): KeyLens<S, T, C, D> = KeyLens { s ->
    val storeA = by(this@times)(s)
    val a = storeA.data
    val storeC = by(other)(a)
    val c = storeC.data
    
    val dToCToB: (D) -> Morphism<C, B> = by(storeC)
    val bToAToT: (B) -> Morphism<A, T> = by(storeA)
    
    KeyStore(c) { d ->
        Morphism { c -> by(bToAToT(by(dToCToB(d))(c)))(a) }
    }
}