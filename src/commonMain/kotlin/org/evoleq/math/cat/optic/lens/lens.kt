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

import org.evoleq.math.cat.aux.o
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
fun <W, P> Lens<W, P>.update(p: P): Morphism<W, W> = setter(p)

@MathCatDsl
fun <W, P> Lens<W, P>.indexed(): ILens<W, W, P, P> = ILens { w ->
    (by(this@indexed)(w)).indexed()
}

fun <W, P> Lens<W, P>.keys(): KeyLens<W, W, P, P> = KeyLens { w ->
    (by(this@keys)(w)).keyStore()
}

operator fun <W, P, Q> Lens<W, P>.times(other: Lens<P, Q>): Lens<W, Q> = (this.indexed() * other.indexed()).toLens()

operator fun <W, P, Q> Lens<W, P>.div(other: Lens<Q, W>): Lens<Q, P> = other * this

@MathCatDsl
fun <W, P, D, E> Lens<W, P>.bypass(f: (D)->E): Lens<Pair<W, D>, Pair<P, E>> = Lens { pair ->
    with(by(this@bypass)(pair.first)) store@{
        Store(Pair(this.data, f(pair.second))) { newPair ->
            Pair(by(this@store)(newPair.first), pair.second)
        }
    }
}
// TODO(adhoc def - move to appropriate place)
typealias Iso<S, T> = Pair<(S)->T, (T)->S>
fun <W, P, Q> Lens<W, P>.times(iso: Iso<P, Q>): Lens<W, Q> = Lens(
    iso.first o by(view())
){
    w: W -> {q:Q -> by(update(iso.second(q)))(w)}
}

operator fun <V, W, P> Iso<V, W>.times(lens: Lens<W, P>): Lens<V, P> = Lens(
    view = by(lens.view()) o first,
    update = {v: V -> {p -> second(by(lens.update(p))(first(v))) }}
)

@MathCatDsl
infix fun <V, W, P, Q> Lens<V, P>.x(other: Lens<W, Q>): Lens<Pair<V, W>, Pair<P, Q>> = Lens(
    {pair -> Pair(by(view())(pair.first), by(other.view())(pair.second)) }
) {
    pair -> {qair -> Pair(
        by(update(qair.first))(pair.first),
        by(other.update(qair.second))(pair.second)
    )}
}

@MathCatDsl
@Suppress("FunctionName")
fun <F, S> First(): Lens<Pair<F, S>, F> = Lens(
    {pair -> pair.first}
){
    pair -> {first -> Pair(first, pair.second)}
}

@MathCatDsl
@Suppress("FunctionName")
fun <F, S> Second(): Lens<Pair<F, S>, S> = Lens(
    {pair -> pair.second}
){
    pair -> {second -> Pair(pair.first, second)}
}


