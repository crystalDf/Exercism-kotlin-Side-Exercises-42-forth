class Forth {

    fun evaluate(vararg line: String): List<Int> =
            line.last().replaceWithWordsDefinition(line.toList().addWordsDefinition()).evaluate()

    private fun List<String>.addWordsDefinition(): Map<String, String> =
            this.filter { it.startsWith(":") }.fold(mapOf()) {
                acc, s -> acc.plus(s.getWordDefinition(acc))
            }

    private fun String.getWordDefinition(wordsDefinition: Map<String, String>): Pair<String, String> {

        val words = this.split(" ")

        val word = words[1]

        if (word.all { it.isDigit() }) throw Exception("illegal operation")

        val definition = (2 until words.size - 1).joinToString(" ") {
            wordsDefinition[words[it]].orEmpty().ifEmpty { words[it] }
        }

        return word to definition
    }

    private fun String.replaceWithWordsDefinition(wordsDefinition: Map<String, String>): String =
            wordsDefinition.keys.fold(this) {
                acc, s -> acc.replace(s, wordsDefinition[s].toString(), true)
            }

    private fun String.evaluate(): List<Int> =
            this.split(" ").fold(emptyList()) {
                acc, s -> acc.evaluate(s)
            }

    private fun List<Int>.evaluate(word: String): List<Int> =
            when(val name = word.toUpperCase()) {

                in Operator.values().map { it.operatorName } -> {

                    val operator = Operator.values().first { it.operatorName == name }

                    when {
                        this.isEmpty() -> throw Exception("empty stack")
                        this.size < operator.requiredValues -> throw Exception("only one value on the stack")
                        else -> operator.operatorFunction(this)
                    }
                }

                else -> {
                    if (!word.all { it.isDigit() }) throw Exception("undefined operation")

                    this.plus(word.toInt())
                }
            }

    enum class Operator(val operatorName: String, val requiredValues: Int,
                        val operatorFunction: (List<Int>) -> List<Int>) {
        ADD("+", 2, { it.calculate { a, b -> a + b } }),
        SUBTRACT("-", 2, { it.calculate { a, b -> a - b } }),
        MULTIPLY("*", 2, { it.calculate { a, b -> a * b } }),
        DIVIDE("/", 2, { it.calculate { a, b -> a / b } }),
        DUP("DUP", 1, { it.plus(it.last()) }),
        DROP("DROP", 1, { it.dropLast(1) }),
        SWAP("SWAP", 2, { it.dropLast(2).plus(it.takeLast(2).reversed())}),
        OVER("OVER", 2, { it.plus(it.takeLast(2).first())});

        companion object {
            private fun List<Int>.calculate(arithmetic: (Int, Int) -> Int): List<Int> =
                    try {
                        this.dropLast(2).plus(arithmetic(this.takeLast(2).first(), this.last()))
                    } catch (e: ArithmeticException) {
                        throw Exception("divide by zero")
                    }
        }
    }
}
