import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class PerformanceTest {

    companion object {
        private const val TEST_COUNT = 5
        private const val THREAD_COUNT = 4
        private const val ARRAY_SIZE = 100_000_000
    }

    @Test
    fun performanceTest() {
        val coroutineDispatcher = Executors.newFixedThreadPool(THREAD_COUNT).asCoroutineDispatcher()
        var seqTime = 0L
        var parTime = 0L
        for (i in 1..TEST_COUNT) {
            val array = ArrayGenerator.randomArray(ARRAY_SIZE)
            val seqCopy = array.copyOf()
            seqTime += countMillis {
                seqCopy.seqQuickSort()
            }
            val parCopy = array.copyOf()
            parTime += countMillis {
                runBlocking {
                    launch(coroutineDispatcher) {
                        parCopy.parQuickSort()
                    }
                }
            }
        }
        println("Sequential sort average time: ${seqTime.toDouble() / TEST_COUNT}")
        println("Parallel sort average time: ${parTime.toDouble() / TEST_COUNT}")
    }

    private fun countMillis(func: () -> Unit): Long {
        val start = System.currentTimeMillis()
        func()
        return System.currentTimeMillis() - start
    }
}