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

import org.evoleq.math.cat.comonad.store.Store
import org.evoleq.math.cat.comonad.store.indexed
import org.evoleq.math.cat.comonad.store.keyStore
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.morphism.Morphism
import org.evoleq.math.cat.morphism.by

typealias Lens<W,P> = Morphism<W, Store<P, W>>

@MathCatDsl
@Suppress("FunctionName")
fun <W,P> Lens(arrow: (W)->Store<P,W>): Lens<W, P> = Morphism(arrow)

@MathCatDsl
@Suppress("FunctionName")
fun <W,P> Lens(view: (W)->P,update: (W)->(P)->W): Lens<W, P> = Lens { w ->
    Store(view(w)) { p -> update(w)(p) }
}

@MathCatDsl
fun <W, P> Lens<W, P>.getter(): Morphism<W, P> = Morphism {
    w ->by(this@getter)(w).data
}
@MathCatDsl
fun <W, P> Lens<W, P>.view(): Morphism<W, P> = getter()

@MathCatDsl
fun <W, P> Lens<W, P>.setter(p:P): Morphism<W, W> = Morphism {
    w -> by(by(this@setter)(w))(p)
}
@MathCatDsl
fun <W, P> Lens<W, P>.setter(): Morphism<W, Morphism<P,W>> = Morphism(morphism)

@MathCatDsl
fun <W, P> Lens<W, P>.update(): Morphism<W, Morphism<P,W>> = setter()

@MathCatDsl
fun <W, P> Lens<W, P>.indexed(): ILens<W, W, P, P> = ILens { w ->
    (by(this@indexed)(w)).indexed()
}

fun <W, P> Lens<W, P>.keys(): KeyLens<W, W, P, P> = KeyLens { w ->
    (by(this@keys)(w)).keyStore()
}

operator fun <W, P, Q> Lens<W, P>.times(other: Lens<P, Q>): Lens<W, Q> = (this.indexed() * other.indexed()).toLens()

operator fun <W, P, Q> Lens<W, P>.div(other: Lens<Q, W>): Lens<Q, P> = other * this
//(this.indexed() / other.indexed()).toLens()


fun <W, P, D, E> Lens<W, P>.bypass(f: (D)->E): Lens<Pair<W, D>, Pair<P, E>> = Lens { pair ->
    with(by(this@bypass)(pair.first)) store@{
        Store(Pair(this.data, f(pair.second))) { newPair ->
            Pair(by(this@store)(newPair.first), pair.second)
        }
    }
}
