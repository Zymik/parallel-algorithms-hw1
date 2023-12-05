import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min
import kotlin.random.Random


/**
 * Попытки написать квик сорт через фильтр.
 * Попытка неудачная
 */
var block = 1000

suspend fun IntArray.parallelSort() = sortArray(this, 0, size)

private suspend fun sortArray(array: IntArray, l: Int, r: Int) {
    if (r - l < block) {
        seqQuicksort(array, l, r - 1)
        return
    }


    val value = array[Random.nextInt(l, r)]
    val lower = array.filterParallel(l, r) { it <= value }
    val greater = array.filterParallel(l, r) { it > value }

    mapIndexedParallel(array, l, r) { _, i ->
        if (i < l + lower.size) {
            lower[i - l]
        } else {
            greater[i - l - lower.size]
        }
    }
    coroutineScope {
        launch {
            sortArray(array, l, l + lower.size)
        }
        launch {
            sortArray(array, l + lower.size, r)
        }
    }
}

private suspend fun IntArray.mapIndexedParallel(func: (Int, Int) -> Int) {
    mapIndexedParallel(this, 0, size, func)
}

suspend fun IntArray.mapParallel(func: (Int) -> Int): IntArray = mapParallel(0, size, func)

private suspend fun IntArray.mapParallel(l: Int, r: Int, func: (Int) -> Int): IntArray {
    val result = IntArray(size)
    mapIndexedParallel(this, l, r) { v, i ->
        result[i - l] = func(v)
        v
    }
    return result
}


suspend fun IntArray.pFor(func: (Int) -> Unit) {
    mapIndexedParallel(this, 0, size) { v, i ->
        func(i)
        v
    }
}


private suspend fun mapIndexedParallel(array: IntArray, l: Int, r: Int, func: (Int, Int) -> Int) {
    if (r - l <= block) {
        for (i in l until r) {
            array[i] = func(array[i], i)
        }
        return
    }
    val m = (l + r) / 2
    coroutineScope {
        launch { mapIndexedParallel(array, l, m, func) }
        launch { mapIndexedParallel(array, m, r, func) }
    }
}

suspend fun IntArray.scan(): IntArray {
    val result = IntArray(size)
    scan(this, result)
    return result
}

private suspend fun scan(input: IntArray, result: IntArray) {
    if (input.size < block) {
        serialScan(input, result)
        return
    }
    var sums = IntArray(ceil(input.size.toDouble() / block).toInt())

    sums.mapIndexedParallel { _, i ->
        val left = block * i
        val right = min(block * (i + 1), input.size)
        input.serialReduce(left, right)
    }

    sums = sums.scan()
    sums.pFor { i ->
        var j = i + 1
        var base = sums[i]
        if (i == sums.size - 1) {
            j = 0
            base = 0
        }

        val left = block * j
        val right = min(block * (j + 1), input.size)
        serialScan(input, result, left, right, base)
    }
}

private fun serialScan(input: IntArray, result: IntArray, l: Int = 0, r: Int = input.size, base: Int = 0) {
    var sum = base
    for (i in l until r) {
        sum += input[i]
        result[i] = sum
    }
}

private fun IntArray.serialReduce(l: Int, r: Int): Int {
    var sum = 0
    for (i in l until r) {
        sum += get(i)
    }
    return sum
}


suspend fun IntArray.filterParallel(func: (Int) -> Boolean): IntArray = filterParallel(0, size, func)

suspend fun IntArray.filterParallel(l: Int, r: Int, func: (Int) -> Boolean): IntArray {
    val filtered = mapParallel(l, r) { if (func(it)) 1 else 0 }
    val scanned = filtered.scan()

    val result = IntArray(scanned.last())
    scanned.pFor { i ->
        if (filtered[i] == 1) {
            result[scanned[i] - 1] = get(l + i)
        }
    }
    return result
}
