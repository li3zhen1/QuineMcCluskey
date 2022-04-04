@file:Suppress("NOTHING_TO_INLINE")

/**
 * a data class maintaining a char array with `equals` override like `String`
 * TODO: change to value class in the future version.
 */
data class Term(
    val chars: CharArray
) : Comparable<Term> {

    constructor(content: String) : this(content.toCharArray())
    constructor(size: Int) : this(CharArray(size))
    constructor(size: Int, predicate: (Int) -> Char) : this(CharArray(size, predicate))

    operator fun get(index: Int): Char = chars[index]

    operator fun set(index: Int, value: Char) {  chars[index] = value }

    val size: Int = chars.size

    operator fun iterator() = chars.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Term) return false
        if (!chars.contentEquals(other.chars)) return false
        return true
    }

    override fun compareTo(other: Term): Int = String(chars).compareTo(String(other.chars))

    override fun hashCode() = chars.contentHashCode()

    override fun toString() = String(chars)

    inline val indices get() = chars.indices

    inline fun count() = chars.count()

    inline fun count(predicate: (Char) -> Boolean): Int {
        var count = 0
        for (element in this) if (predicate(element)) ++count
        return count
    }

    inline fun forEachIndexed(predicate: (Int, Char) -> Unit) = chars.forEachIndexed(predicate)

    inline fun copyOf() = Term(chars.copyOf())

    inline fun <T> map(transform: (Char) -> T) = chars.map(transform)

    inline fun <T> mapIndexed(transform: (Int, Char) -> T) = chars.mapIndexed(transform)

    inline fun zip(other: Term) = chars.zip(other.chars)

    inline fun forEach(action: (Char) -> Unit) = chars.forEach(action)

    inline fun replace(from: Char, to: Char) = this.apply {
        indices.forEach {
            if (this[it] == from) this[it] = to
        }
    }

    inline fun getIndicesOf(char: Char) = getIndicesFromTerm(this, char)

    inline fun getTermFromImplicants() = listOf('1', '0', '^', '~', '-').map { this.getIndicesOf(it).toList() }

    inline fun getComplexity() = this.getTermFromImplicants().run {
        this[0].size + this[1].size * 1.5 + this[2].size * 1.25 + this[3].size * 1.75
    }

    inline fun permutationExcluding(exclude: Set<Term> = setOf()) = permutations(this, exclude)

    companion object {

        fun getIndicesFromTerm(term: Term, char: Char) = sequence {
            term.forEachIndexed { i, c -> if (c == char) yield(i) }
        }


        fun permutations(value: Term, exclude: Set<Term> = setOf()) = sequence {
            val nBits = value.size
            val nXor = value.count { it == '^' || it == '~' }
            var xorValue = 0
            var seenXor = 0
            val res = Term(nBits) { '0' } //value.map { '0' }.toMutableList()
            var i = 0
            var direction = +1

            while (i >= 0) {
                when (value[i]) {
                    '1', '0' -> res[i] = value[i]
                    '-' -> {
                        if (direction == +1) {
                            res[i] = '0'
                        } else if (res[i] == '0') {
                            res[i] = '1'
                            direction = +1
                        }
                    }
                    '^' -> {
                        seenXor += direction
                        if (direction == +1) {
                            res[i] = if (seenXor == nXor && xorValue == 0) '1' else '0'
                        } else if (res[i] == '0' && seenXor < nXor - 1) {
                            res[i] = '1'
                            direction = +1
                            seenXor += 1
                        }
                        if (res[i] == '1') {
                            xorValue = xorValue xor 1
                        }
                    }
                    '~' -> {
                        seenXor += direction
                        if (direction == +1) {
                            res[i] = if (seenXor == nXor && xorValue == 1) '1' else '0'
                        } else if (res[i] == '0' && seenXor < nXor - 1) {
                            res[i] = '1'
                            direction = +1
                            seenXor += 1
                        }
                        if (res[i] == '1') {
                            xorValue = xorValue xor 1
                        }
                    }
                    else -> res[i] = '#'
                }
                i += direction
                if (i == nBits) {
                    direction = -1
                    i = nBits - 1
                    if (!exclude.contains(res)) {
                        yield(res.copyOf())
                    }
                }
            }
        }.toList()
    }
}


inline fun Int.toTerm(bits: Int) = Integer.toBinaryString(this).run {
    if (bits >= length)
        Term(this.toCharArray(CharArray(bits) { '0' }, bits - length))
    else
        Term(this.toCharArray(CharArray(bits) { '0' }, 0, length - bits, length))
}


fun combine(a: Term, b: Term, dontCares: Set<Term>): Term? {
    val permutationsA = a.permutationExcluding(dontCares).toSet()
    val permutationsB = b.permutationExcluding(dontCares).toSet()
    val aTermDontCares = a.getIndicesOf('-').toList()
    val bTermDontCares = b.getIndicesOf('-').toList()
    val aPotential = a.copyOf()
    val bPotential = b.copyOf()
    aTermDontCares.indices.forEach { aPotential[it] = b[it] }
    bTermDontCares.indices.forEach { bPotential[it] = a[it] }
    val valid = listOf(aPotential, bPotential).filter {
        it.permutationExcluding(dontCares) == permutationsA + permutationsB
    }
    if (valid.isEmpty()) return null
    return valid.sortedBy { it.getComplexity() }[0]
}
