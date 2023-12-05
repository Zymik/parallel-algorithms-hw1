import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.RepeatedTest

/**
 * Тесты для проверки корректности алгоритмов
 */
class CorrectnessTest {
    private fun IntArray.contentEquals(expected: IntArray) {
        assert(size == expected.size) { "Arrays have different size" }

        for (i in indices) {
            assert(get(i) == expected[i]) { "Different elements at index $i" }
        }
    }

    @RepeatedTest(100)
    fun sequentialQuickSortTest() {
        val array = ArrayGenerator.randomArray(100)
        val sorted = array.sorted().toIntArray()
        array.seqQuickSort()
        array.contentEquals(sorted)
    }

    @RepeatedTest(100)
    fun parallelQuickSortTest() {
        val array = ArrayGenerator.randomArray(100)
        val sorted = array.sorted().toIntArray()
        runBlocking {
            launch {
                array.parQuickSort()
            }
        }
        array.contentEquals(sorted)
    }

    private fun IntArray.sequentialScan(): IntArray {
        var sum = 0
        val result = IntArray(size)
        for (i in indices) {
            sum += get(i)
            result[i] = sum
        }
        return result
    }

    @RepeatedTest(100)
    fun testMap() {
        block = 10
        val r = ArrayGenerator.randomArray(100)
        val result = runBlocking {
            r.mapParallel { v -> v % 2 }
        }
        val seqResult = r.map { v -> v % 2 }.toIntArray()

        result.contentEquals(seqResult)
    }

    @RepeatedTest(100)
    fun testScan() {
        block = 10
        val array = ArrayGenerator.randomArray(100)
        val result = runBlocking {
            array.scan()
        }

        result.contentEquals(array.sequentialScan())
    }

    @RepeatedTest(100)
    fun testFilter() {
        block = 10
        val array = ArrayGenerator.randomArray(100)
        val result = runBlocking {
            array.filterParallel { it % 2 == 0 }
        }
        val seqResult = array.filter { it % 2 == 0 }.toIntArray()
        result.contentEquals(seqResult)
    }

    @RepeatedTest(100)
    fun parallelSortTest() {
        block = 10
        val array = ArrayGenerator.randomArray(100)
        val sorted = array.sorted().toIntArray()
        println()
        runBlocking {
            launch { array.parallelSort() }
        }
        array.contentEquals(sorted)
    }

}