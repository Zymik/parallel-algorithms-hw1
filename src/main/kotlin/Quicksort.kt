import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

fun IntArray.seqQuickSort() = seqQuicksort(this, 0, size - 1)

suspend fun IntArray.parQuickSort(blockSize: Int = 2500) = parQuickSort(this, 0, size - 1, blockSize)

suspend fun parQuickSort(array: IntArray, l: Int, r: Int, blockSize: Int) {
    if (r - l < blockSize) {
        seqQuicksort(array, l, r)
        return
    }
    val q = partition(array, l, r)

    coroutineScope {
        launch { parQuickSort(array, l, q, blockSize) }
        launch { parQuickSort(array, q + 1, r, blockSize) }
    }
}

fun seqQuicksort(array: IntArray, l: Int, r: Int) {
    if (r <= l) {
        return
    }
    val q = partition(array, l, r)
    seqQuicksort(array, l, q)
    seqQuicksort(array, q + 1, r)
}

private fun partition(array: IntArray, l: Int, r: Int): Int {
    val index = Random.nextInt(l, r + 1)
    val value = array[index]

    var i = l
    var j = r
    while (i <= j) {
        while (array[i] < value) {
            i++
        }
        while (array[j] > value) {
            j--
        }
        if (i > j) {
            break
        }
        val t = array[i]
        array[i] = array[j]
        array[j] = t
        i++
        j--
    }
    return j
}