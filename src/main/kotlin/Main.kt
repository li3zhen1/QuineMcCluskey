
fun main(args: Array<String>) {
    val simplified = QuineMcCluskey().simplify(intArrayOf(3, 4, 5, 7, 9, 13, 14, 15))
    assert(simplified == setOf("010-", "1-01", "0-11", "111-"))
    println(simplified)
}