package com.example.rapidrecall

class Attempt (val correctSequence: String, val userSequence: String,
               val timestamp: Long = System.currentTimeMillis()) {

    fun isCorrect(): Boolean {
        return correctSequence == userSequence
    }

    fun getUserSeq(): String {
        return userSequence
    }

    fun getCorrectSeq(): String {
        return correctSequence
    }
}