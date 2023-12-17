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

    val transformationMaps = seedsAndMaps.drop(1).map { transformationMap ->
        transformationMap.map { line -> line.split(" ").map { it.toLong() } }
    }

    val locations = transformationMaps.fold(seeds) { transformedSeeds, transformationMap ->
        transformedSeeds.map { it.transform(transformationMap) }
    }

    println(locations.min())
}

fun Long.transform(transformationMap: List<List<Long>>): Long =
    transformationMap.find { (_, sourceRangeStart, rangeLength) ->
        this in sourceRangeStart..<sourceRangeStart + rangeLength
    }?.let { (destinationRangeStart, sourceRangeStart, _) ->
        val offset = this - sourceRangeStart
        destinationRangeStart + offset
    } ?: this
