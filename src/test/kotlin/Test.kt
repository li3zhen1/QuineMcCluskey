import kotlin.test.Test

fun Set<Term>.validate(vararg strings: String) = strings.map { Term(it) }.toSet().equals(this)

class Test {

    @Test
    fun test1() {
        val res = QuineMcCluskey.simplify(1, 2, 5, 6, 9, 10, 13, 14)
        println(res)
        assert(res.validate("--01", "--10"))
    }

    @Test
    fun test2() {
        val res = QuineMcCluskey.simplify(2, 6, 10, 14)
        println(res)
        assert(res.validate("--10"))
    }

    @Test
    fun test3() {
        val res = QuineMcCluskey.simplify(3, 4, 5, 7, 9, 13, 14, 15)
        println(res)
        assert(res.validate("010-", "1-01", "111-", "0-11"))
    }

    @Test
    fun testDc() {
        val res = QuineMcCluskey.simplify(
            intArrayOf(1, 3, 5, 7, 9, 11, 13, 15),
            intArrayOf(0, 2, 4, 6, 8, 10, 12, 14)
        )
        println(res)
        assert(res.validate("----"))
    }
}