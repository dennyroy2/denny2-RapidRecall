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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
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
import com.example.rapidrecall.ui.theme.GameViewModel
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RapidRecallTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RapidRecallApp(viewModel)
                }
            }
        }
    }
}

enum class Screen {
    INIT, START, LOG, SUMMARY;
}

@Composable
fun RapidRecallApp(viewModel: GameViewModel) {

    when (viewModel.currentScreen) {
        Screen.INIT -> StartScreen(onNavigate = { newScreen -> viewModel.changeScreen(newScreen) })
        Screen.START -> GameScreen(viewModel)
        Screen.LOG -> LogScreen(viewModel)
        Screen.SUMMARY -> SummaryScreen(viewModel)
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
    SETUP, SEQUENCE, INPUT, FEEDBACK
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
    viewModel: GameViewModel
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BackHandler { viewModel.goBack() }
        when (viewModel.phase) {
            GameScreenPhase.SETUP -> GameScreenSetup(
                length = viewModel.length,
                onLengthChange = { viewModel.updateLength(it) },
                onStart = { viewModel.startRound() }
            )
            GameScreenPhase.SEQUENCE -> GameScreenSequence(currentDigit = viewModel.currentDigit)

            GameScreenPhase.INPUT -> GameScreenInput(viewModel.guess,
                { viewModel.updateGuess(it) },
                {viewModel.submitGuess()})

            GameScreenPhase.FEEDBACK -> GameScreenFeedback(viewModel.sequence, viewModel.guess
            ) { viewModel.resetRound() }

        }



    }
}

@Composable
fun GameScreenFeedback(
    sequence: String,
    guess: String,
    reset: () -> Unit
) {
    var text: String
    text = if (sequence == guess) {
        "Correct answer!"
    } else {
        "Incorrect answer."
    }

    Text(text)
    Text("Correct sequence: $sequence")
    Text("Your sequence: $guess")
    Button (
        onClick = {reset()}
    ) {
        Text("New Attempt")
    }
}
@Composable
fun GameScreenInput(
    guess: String,
    updateGuess: (String) -> Unit,
    submitGuess: () -> Unit
) {
    Text("Enter the sequence:")
    OutlinedTextField(
        value = guess,
        onValueChange = { newText ->
            updateGuess(newText) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Button(
        onClick = { submitGuess() }
    ) {
        Text("Submit")
    }
}
@Composable
fun GameScreenSequence(currentDigit: String) {
    Text(currentDigit, fontSize = 96.sp)
}

@Composable
fun GameScreenSetup(
    length: Int,
    onLengthChange: (Int) -> Unit,
    onStart: () -> Unit
) {
        Text("Sequence Length: $length")
        Slider(
            value = length.toFloat(),
            onValueChange = { onLengthChange(it.roundToInt()) },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.padding(20.dp)
        )
        Button(
            onClick = onStart
        ) {
            Text("GENERATE SEQUENCE")
        }
}

@Composable
fun LogScreen(
    viewModel: GameViewModel
) {
    BackHandler { viewModel.goBack() }

    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (viewModel.attempts.isEmpty()) {
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
                itemsIndexed(viewModel.attempts) {index, attempt ->
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
    viewModel: GameViewModel,
) {
    BackHandler { viewModel.goBack() }
    var index = 0

    val rows = viewModel.getSummaryRows()

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