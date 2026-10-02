package com.example.rapidrecall.ui.theme


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.rapidrecall.Screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.rapidrecall.Attempt
import com.example.rapidrecall.GameScreenPhase
import com.example.rapidrecall.RandomGenerator
import com.example.rapidrecall.Summary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


class GameViewModel : ViewModel() {
    var currentScreen by mutableStateOf(Screen.INIT)
        private set
    fun changeScreen(newScreen: Screen) {
        currentScreen = newScreen
    }
    val attempts = mutableStateListOf<Attempt>()

    fun goBack() {
        currentDigit = ""
        guess = ""
        phase = GameScreenPhase.SETUP
        currentScreen = Screen.INIT
    }

    var length by mutableIntStateOf(5)
        private set

    private val rand = RandomGenerator()

    var sequence = ""
        private set
    var phase by mutableStateOf(GameScreenPhase.SETUP)
        private set

    var currentDigit by mutableStateOf("")
        private set

    var guess by mutableStateOf("")

    private var showingJob: Job? = null

    fun updateLength(newLength: Int) {
        if (newLength > 10) {length = 10}
        if (newLength < 1) {length = 0}
        length = newLength

    }

    fun startRound() {
        sequence = rand.getRandomSequence(length)
        guess = ""
        phase = GameScreenPhase.SEQUENCE
        showingJob = viewModelScope.launch{
            for (digit in sequence) {
                currentDigit = digit.toString()
                delay(1000.milliseconds)
                currentDigit = ""
                delay(250.milliseconds)

            }
            phase = GameScreenPhase.INPUT
        }
    }

    fun updateGuess(text: String) {
        if (text.length <= length && text.all {it.isDigit()}) {
            guess = text
        }
    }

    val canSubmit: Boolean get() = length == guess.length

    fun submitGuess() {
        if (!canSubmit) return
        val attempt = Attempt(sequence,guess)
        attempts.add(attempt)
        phase = GameScreenPhase.FEEDBACK
    }

    fun resetRound() {
        currentDigit = ""
        guess = ""
        phase = GameScreenPhase.SETUP
    }

    fun getSummaryRows(): LinkedHashMap<String, String> {

        val curSummary = Summary(attempts)
        curSummary.calculate()
        val rows = linkedMapOf(
            "Attempts" to attempts.size.toString(),
            "Correct" to curSummary.correct.toString(),
            "Win Rate" to curSummary.winRate(),
            "Largest Correct Sequence" to curSummary.largestSeq,
            "Size" to curSummary.largestCorrectlyGuessed.toString())
        return rows
    }



}