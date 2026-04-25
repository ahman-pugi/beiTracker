package com.ahmanpg.beitracker.ui.components


fun generateFakeHistory(currentPrice: Double): List<Double> {
    val list = mutableListOf<Double>()
    var price = currentPrice

    repeat(20) {
        val change = (-0.03..0.03).random()
        price += price * change
        list.add(price)
    }

    return list
}

// extension for random double
fun ClosedFloatingPointRange<Double>.random() =
    (start + Math.random() * (endInclusive - start))
