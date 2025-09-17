package pl.pb.optigai.utils

class BrailleActivity {
    data class BrailleChar(val x: Float, val y: Float, val clazz: String, val height: Float)

    companion object {
        private val numberMap = mapOf(
            "A" to "1", "B" to "2", "C" to "3", "D" to "4", "E" to "5",
            "F" to "6", "G" to "7", "H" to "8", "I" to "9", "J" to "0"
        )

        fun decode(predictions: List<BrailleChar>): String {
            if (predictions.isEmpty()) return ""

            val avgCharHeight = predictions.map { it.height }.average().toFloat()
            val lineTolerance = avgCharHeight * 0.6f
            val lines = mutableListOf<MutableList<BrailleChar>>()
            val sortedChars = predictions.sortedWith(compareBy({ it.y }, { it.x }))

            for (char in sortedChars) {
                val line = lines.find { existingLine ->
                    val avgY = existingLine.map { it.y }.average().toFloat()
                    kotlin.math.abs(avgY - char.y) < lineTolerance
                }
                if (line != null) {
                    line.add(char)
                } else {
                    lines.add(mutableListOf(char))
                }
            }
            lines.forEach { line -> line.sortBy { it.x } }

            val sb = StringBuilder()
            var isNumberMode = false

            for ((lineIndex, line) in lines.withIndex()) {
                var prevX: Float? = null
                var avgGap = 0f

                if (line.size > 1) {
                    avgGap = line.zipWithNext { a, b -> b.x - a.x }.average().toFloat()
                }
                for (char in line) {
                    val clazz = char.clazz

                    if (clazz.equals("NUMBER_PREFIX", ignoreCase = true)) {
                        isNumberMode = true
                        continue
                    }
                    if (prevX != null && avgGap > 0f) {
                        if (char.x - prevX > avgGap * 1.5f) sb.append(" ")
                    }
                    if (isNumberMode && numberMap.containsKey(clazz)) {
                        sb.append(numberMap[clazz])
                    } else {
                        sb.append(clazz)
                    }
                    prevX = char.x
                }
                if (lineIndex < lines.size - 1) {
                    sb.append(" ")
                }
            }
            return sb.toString().trim()
        }
    }
}
