package com.example.rapidrecall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.rapidrecall.ui.theme.RapidRecallTheme
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.BiasAlignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RapidRecallTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RapidRecallApp()
                }
            }
        }
    }
}

enum class Screen {
    INIT, START, LOG, SUMMARY;
}

@Composable
fun RapidRecallApp() {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.INIT) }
    val attempts = remember { mutableStateListOf<Attempt>() }
    val goBack = { currentScreen = Screen.INIT}

    when (currentScreen) {
        Screen.INIT -> StartScreen(onNavigate = { newScreen -> currentScreen = newScreen })
        Screen.START -> GameScreen(
            onAttemptSubmitted = {attempts.add(it)},
            onBack = goBack
        )
        Screen.LOG -> LogScreen(attempts, onBack = goBack)
        Screen.SUMMARY -> SummaryScreen(attempts, onBack = goBack)
    }

}
@Composable
fun StartScreen(
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ){
        Text("Rapid Recall",
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            modifier = Modifier.align(BiasAlignment(horizontalBias = 0f, verticalBias = -0.6f)))

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        )
        {
            StartScreenButton("Start", onClick = { onNavigate(Screen.START) })
            StartScreenButton("Log", onClick = { onNavigate(Screen.LOG) })
            StartScreenButton("Summary", onClick = { onNavigate(Screen.SUMMARY) })
        }
        Text("Name: Denny Roy | CCID: denny2",
            modifier = Modifier.align(Alignment.BottomCenter))
    }

}

@Composable
fun StartScreenButton(
    buttonName: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor = if (isPressed) Color.LightGray else Color.White
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button (
            onClick = onClick,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = Color.Black),
            modifier = Modifier.width(200.dp)
        ) {
            Text(buttonName)
        }
    }
}

enum class GameScreenPhase {
    SETUP, SEQUENCE, INPUT
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
) {
        Text(
            text = text,
            modifier = Modifier
                .weight(weight)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
    }


@Composable
fun GameScreen(
    onAttemptSubmitted: (Attempt) -> Unit,
    onBack: () -> Unit
) {

    var length by remember { mutableIntStateOf(5)}
    val rand = RandomGenerator()
    var sequence by remember { mutableStateOf("") }
    var phase by remember {mutableStateOf(GameScreenPhase.SETUP)}
    var guess by remember {mutableStateOf("")}
    var currentDigit by remember {mutableStateOf("")}
    var text by remember { mutableStateOf("") }
    var newAttempt by remember { mutableStateOf(false)}

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BackHandler { onBack() }
        when (phase) {
            GameScreenPhase.SETUP ->{
                Text("Sequence Length: $length")
                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.roundToInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.padding(20.dp)
                )

                Button(
                    onClick = {
                        sequence = rand.getRandomSequence(length)
                        phase = GameScreenPhase.SEQUENCE}
                ) {
                    Text("GENERATE SEQUENCE")
                }
            }
            GameScreenPhase.SEQUENCE -> {
                LaunchedEffect(sequence) {
                    for (char in sequence) {
                        currentDigit = char.toString()
                        delay(1000.milliseconds)
                        currentDigit = ""
                        delay(250.milliseconds)
                    }
                    phase = GameScreenPhase.INPUT
                }
                Text(currentDigit, fontSize = 96.sp)
            }

            GameScreenPhase.INPUT -> {
                if (!newAttempt){
                    Text("Enter the Sequence:")
                    OutlinedTextField(
                        value = guess,
                        onValueChange = { newText ->
                            if (newText.length <= length) {
                                guess = newText
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = {
                            val curAttempt = Attempt(sequence, guess)
                            onAttemptSubmitted(curAttempt)
                            newAttempt = true
                            text = if (curAttempt.isCorrect()) {
                                "Correct guess!"
                            } else {
                                "Incorrect guess."
                            }
                        },
                        enabled = guess.length == length
                    ) {
                        Text("Submit")
                    }
                }
                if (text.isNotEmpty()) {
                    Text(text)
                    Text("Correct sequence: $sequence")
                    Text("Your sequence: $guess")
                }

                if (newAttempt) {
                    Button(
                        onClick = {
                            phase = GameScreenPhase.SETUP
                            guess = ""
                            text = ""
                            newAttempt = false
                        }

                    ) {
                        Text("New Attempt")
                    }
                }
            }

        }



    }
}

@Composable
fun LogScreen(
    attempts: List<Attempt>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (attempts.isEmpty()) {
            Text("No attempts yet.")
        } else {
            Row(Modifier.background(MaterialTheme.colorScheme.primary)) {
                TableCell("#", 0.5f, true)
                TableCell("Correct", 1.5f, true)
                TableCell("Input", 1.5f, true)
                TableCell("Result", 1.5f, true)
                TableCell("Time", 1.5f, true)
            }
            LazyColumn {
                itemsIndexed(attempts) {index, attempt ->
                    val rowColor = if (index % 2 == 0) Color.Transparent else
                        MaterialTheme.colorScheme.surfaceVariant
                    val time = formatter.format(Date(attempt.timestamp))
                    Row(modifier =Modifier.background(rowColor)) {
                        TableCell("${index+1}", 0.5f)
                        TableCell(attempt.getCorrectSeq(), 1.5f)
                        TableCell(attempt.getUserSeq(), 1.5f)
                        TableCell("${attempt.isCorrect()}", 1.25f)
                        TableCell("$time", 1.5f)

                    }

                }
            }
        }
    }
}

@Composable
fun SummaryScreen(
    attempts: List<Attempt>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val curSummary = Summary(attempts)
    curSummary.calculate()
    var index = 0

    val rows = linkedMapOf(
        "Attempts" to attempts.size,
        "Correct" to curSummary.correct,
        "Win Rate" to curSummary.winRate(),
        "Largest Correct Sequence" to curSummary.largestSeq,
        "Size" to curSummary.largestCorrectlyGuessed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (row in rows.keys) {
            index += 1
            val rowColor = if (index % 2 == 0) Color.Transparent else
                MaterialTheme.colorScheme.surfaceVariant
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(rowColor)
            ) {
                TableCell(row, 1.5f, true)
                TableCell("${rows[row]}", 1.5f)

            }
        }
    }
}