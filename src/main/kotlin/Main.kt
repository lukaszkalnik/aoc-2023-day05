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

    val transformationMapStages = seedsAndMaps.drop(1).map { transformationMap ->
        transformationMap.map { line ->
            val (destinationRangeStart, sourceRangeStart, rangeLength) = line.split(" ").map { it.toLong() }
            TransformationMap(
                destinationRangeStart = destinationRangeStart,
                sourceRangeStart = sourceRangeStart,
                rangeLength = rangeLength,
            )
        }.sortedBy { it.sourceRangeStart }
    }

    val locations = transformationMapStages.fold(seeds) { transformedSeeds, transformationMap ->
        transformedSeeds.flatMap { it.transform(transformationMap) }
    }

    println(locations.minOf { it.first })
}

data class TransformationMap(
    val destinationRangeStart: Long,
    val sourceRangeStart: Long,
    val rangeLength: Long,
)

fun LongRange.transform(transformationMaps: List<TransformationMap>): List<LongRange> {
    val intersectingTransformationMaps = transformationMaps.filter {
        first < it.sourceRangeStart + it.rangeLength && last >= it.sourceRangeStart
    }.map {
        val offset = it.destinationRangeStart - it.sourceRangeStart
        val intersectingSourceRangeStart = it.sourceRangeStart.coerceAtLeast(first)
        val intersectingDestinationRangeStart = intersectingSourceRangeStart + offset
        val intersectingRangeLength = it.rangeLength.coerceAtMost(last - intersectingSourceRangeStart + 1)
        TransformationMap(
            destinationRangeStart = intersectingDestinationRangeStart,
            sourceRangeStart = intersectingSourceRangeStart,
            rangeLength = intersectingRangeLength,
        )
    }

    return buildList {
        intersectingTransformationMaps.fold(first) { first, transformationMap ->
            if (first < transformationMap.sourceRangeStart) add(first until transformationMap.sourceRangeStart)
            val destinationRangeEnd = transformationMap.destinationRangeStart + transformationMap.rangeLength
            add(transformationMap.destinationRangeStart until destinationRangeEnd)
            if (destinationRangeEnd < last) add(destinationRangeEnd until last)
            last
        }
    }
}