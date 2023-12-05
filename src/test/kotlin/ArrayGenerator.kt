import kotlin.random.Random

object ArrayGenerator {
    fun randomArray(size: Int, maxInt: Int = Int.MAX_VALUE): IntArray {
        return IntArray(size) { Random.nextInt(maxInt) }
    }
}