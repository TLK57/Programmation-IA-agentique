package com.soywiz.korma.geom
// Bouchon minimal de korma 3.4.0 : égalité sur x et y.
data class Point(val x: Double, val y: Double) {
    constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())
}
