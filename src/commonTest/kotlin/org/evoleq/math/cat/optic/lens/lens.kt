package org.evoleq.math.cat.optic.lens

import kotlin.test.Test

class  LensTest {

    @Test
    fun x() {

        data class Person(val name: String)
        data class Address(val street: String, val city: String)

        val Name = Lens<Person, String>(
            {person -> person.name}
        ) {
            person -> {name -> person.copy(name = name)}
        }

        val Street = Lens<Address, String>(
            {addres -> addres.street}
        ) {
            address -> {street -> address.copy(street = street)}
        }

        val Pair = Name x Street

        val name = Pair * First()

    }

}