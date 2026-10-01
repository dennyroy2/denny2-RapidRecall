package com.example.rapidrecall

class Summary(attempts: List<Attempt>) {
    var correct = 0
    var largestCorrectlyGuessed = 0
    var largestSeq = ""

    val attemptsList = attempts

    fun calculate(){
        for (attempt in attemptsList) {
            if (attempt.isCorrect()) {
                correct += 1
                if (attempt.getCorrectSeq().length > largestCorrectlyGuessed) {
                    largestCorrectlyGuessed = attempt.getCorrectSeq().length
                    largestSeq = attempt.getCorrectSeq()
                }
            }
        }
    }

    fun winRate(): String {
        if (attemptsList.isNotEmpty()) {
            return ((correct.toFloat()/attemptsList.size)*100).toString() + "%"
        }
        else {
            return "0"
        }
    }

}