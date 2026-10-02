package com.example.rapidrecall
import kotlin.random.Random

class RandomGenerator {

    fun getRandomSequence(numDigits: Int): String {
        var sequence = ""

        for (i in 0 until numDigits) {
            sequence += Random.nextInt(0, 10).toString()
        }
        return sequence
    }
}