package org.example

import java.io.File

fun main() {
    val input = File("input.txt").readLines()

    val sliceIndices = input.flatMapIndexed { index, line ->
        when {
            index == 0 || index == input.lastIndex -> listOf(index)
            line.isEmpty() -> listOf(index - 1, index + 2)
            else -> emptyList()
        }
    }.windowed(size = 2, step = 2)

    val seedsAndMaps = sliceIndices.map { (from, to) -> input.slice(from..to) }

    val seeds = seedsAndMaps[0][0].split(": ")[1].split(" ").map { it.toLong() }
        .windowed(size = 2, step = 2)
        .map { (rangeStart, rangeLength) -> rangeStart..<rangeStart + rangeLength }

    val transformationMapsReversed = seedsAndMaps.drop(1).map { transformationMap ->
        transformationMap.map { line -> line.split(" ").map { it.toLong() } }
    }.reversed()

    var seed = 0L
    var location = 0L

    while (true) {
        transformationMapsReversed.forEach {
            seed = seed.transformBackwards(it)
        }
        if (seeds.any { seed in it }) break
        ++location
        seed = location
    }

    println(location)
}

fun Long.transformBackwards(transformationMap: List<List<Long>>): Long =
    transformationMap.find { (destinationRangeStart, _, rangeLength) ->
        this in destinationRangeStart..<destinationRangeStart + rangeLength
    }?.let { (destinationRangeStart, sourceRangeStart, _) ->
        val offset = this - destinationRangeStart
        sourceRangeStart + offset
    } ?: this