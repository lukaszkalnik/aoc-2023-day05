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

    val minLocation = locations.minOf { it.first }
    println(minLocation)
    check(minLocation == 7873084L)
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
        if (intersectingTransformationMaps.isEmpty()) {
            add(first..last)
        } else {
            val lastTransformed = intersectingTransformationMaps.fold(first) { first, transformationMap ->
                val sourceRangeStart = transformationMap.sourceRangeStart
                if (first < sourceRangeStart) add(first until sourceRangeStart)

                val destinationRangeStart = transformationMap.destinationRangeStart
                val rangeLength = transformationMap.rangeLength
                val destinationRangeEnd = destinationRangeStart + rangeLength

                add(destinationRangeStart until destinationRangeEnd)

                val sourceRangeLast = sourceRangeStart + rangeLength - 1
                sourceRangeLast
            }
            if (last > lastTransformed) add(lastTransformed + 1..last)
        }
    }
}