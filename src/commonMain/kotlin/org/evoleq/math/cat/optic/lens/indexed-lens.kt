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

import org.evoleq.math.cat.comonad.store.IStore
import org.evoleq.math.cat.comonad.store.toStore
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.morphism.Morphism
import org.evoleq.math.cat.morphism.by

typealias ILens<S,T,A,B> = Morphism<S,IStore<A,B,T>>


fun <S, T, A, B, TPrime> ILens<S, T, A, B>.map(f: (T)->TPrime): ILens<S, TPrime, A, B> = ILens { s ->
    by(this@map)(s).map(f)
}

fun <S, T, A, B> ILens<S, T, A, B>.getter(): Morphism<S, A> = Morphism {
    s -> by(this@getter)(s).data
}
fun <S, T, A, B> ILens<S, T, A, B>.setter() =  Morphism{
    s: S -> by(Morphism{ b: B -> by(this@setter)(s).morphism(b)})
}

@Suppress("FunctionName")
@MathCatDsl
fun <S,T,A,B> ILens(arrow: (S)->IStore<A,B,T>): ILens<S, T, A, B> = Morphism(arrow)

@Suppress("FunctionName")
@MathCatDsl
fun <S,T,A,B> ILens(view: (S)->A, update: (Pair<S, B>)->T): ILens<S, T, A, B> = ILens { s ->
    IStore(view(s)) { b ->
        update(Pair(s, b))
    }
}

@MathCatDsl
fun <S, A> ILens<S, S, A, A>.toLens(): Lens<S, A> = Lens { w ->
    by(this@toLens)(w).toStore()
}

operator fun <S, T, A, B, C, D> ILens<S, T, A, B>.times(other: ILens<A, B, C, D>): ILens<S, T, C, D> = ILens { s: S ->
    // derive stores and their data
    val storeA = by(this@times)(s)
    val a = storeA.data
    val storeC = by(other)(a)
    val c = storeC.data
    
    val dToB: (D) -> B = by(storeC)
    val bToT: (B) -> T = by(storeA)
    
    IStore(c) { d ->
        bToT(dToB(d))
    }
    
}

operator fun <K, L, S, T, A, B> ILens<S, T, A, B>.div(other: ILens<K, L, S, T>): ILens<K, L, A, B> = other * this
