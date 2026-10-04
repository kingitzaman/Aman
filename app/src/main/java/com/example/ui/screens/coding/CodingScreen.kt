package com.example.ui.screens.coding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.coding.CodeExecutionService
import com.example.data.coding.CodeFile
import com.example.data.coding.ExecutionResult
import com.example.ui.components.AmanixTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.glassmorphic
import com.example.ui.components.keyboardAndNavigationBottomPadding
import com.example.ui.theme.AmanixAccentAmber
import com.example.ui.theme.AmanixAccentGreen
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.launch

@Composable
fun CodingScreen(
    codeExecutionService: CodeExecutionService,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val files = remember {
        mutableStateListOf(
            CodeFile(
                "main.py",
                "python",
                "def calculate_fibonacci(n: int):\n    a, b = 0, 1\n    for _ in range(n):\n        yield a\n        a, b = b, a + b\n\nif __name__ == '__main__':\n    print('Amanix Code Engine')\n    print(list(calculate_fibonacci(10)))\n"
            ),
            CodeFile(
                "Solution.kt",
                "kotlin",
                "fun main() {\n    val items = listOf(\"Think\", \"Search\", \"Create\", \"Grow\")\n    println(\"Amanix: \" + items.joinToString(\" • \"))\n}\n"
            ),
            CodeFile(
                "index.js",
                "javascript",
                "// Amanix Async Pipeline\nasync function run() {\n  console.log('System online');\n}\nrun();\n"
            )
        )
    }

    var selectedFileIndex by remember { mutableIntStateOf(0) }
    var currentCode by remember { mutableStateOf(files[0].content) }
    var executionResult by remember { mutableStateOf<ExecutionResult>(ExecutionResult.Idle) }
    var isCopied by remember { mutableStateOf(false) }

    fun switchFile(index: Int) {
        files[selectedFileIndex] = files[selectedFileIndex].copy(content = currentCode)
        selectedFileIndex = index
        currentCode = files[index].content
    }

    Scaffold(
        topBar = {
            AmanixTopBar(
                title = "Coding & Debugging",
                onMenuClick = onOpenDrawer,
                onProfileClick = onOpenSettings,
                statusText = files[selectedFileIndex].language.uppercase(),
                isStatusOk = true
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = keyboardAndNavigationBottomPadding())
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Glassmorphic File Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                files.forEachIndexed { index, file ->
                    val isSelected = selectedFileIndex == index
                    Box(
                        modifier = Modifier
                            .glassmorphic(
                                shape = RoundedCornerShape(10.dp),
                                backgroundColor = if (isSelected) Color(0x45003E5C) else Color(0x20142338),
                                borderColor = if (isSelected) AmanixCyanPrimary else Color(0x25FFFFFF)
                            )
                            .clickable { switchFile(index) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = file.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AmanixCyanPrimary else AmanixTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glassmorphic Action Toolbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(10.dp),
                        backgroundColor = Color(0x350F1B2E),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    executionResult = ExecutionResult.Running
                                    val activeFile = files[selectedFileIndex]
                                    executionResult = codeExecutionService.executeCode(activeFile.language, currentCode)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmanixCyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(8.dp))
                                .testTag("run_code_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentCode))
                                isCopied = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(8.dp))
                                .testTag("copy_code_button")
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isCopied) AmanixAccentGreen else AmanixTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isCopied) "Copied" else "Copy", fontSize = 12.sp, color = AmanixTextSecondary)
                        }
                    }

                    Text(
                        text = "Isolated Sandbox",
                        fontSize = 11.sp,
                        color = AmanixTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glassmorphic Code Editor Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .glassmorphic(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0x40060C18),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    val lineCount = currentCode.lines().size
                    Column(modifier = Modifier.padding(end = 12.dp)) {
                        for (i in 1..lineCount) {
                            Text(
                                text = i.toString().padStart(2, ' '),
                                color = AmanixTextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    BasicTextField(
                        value = currentCode,
                        onValueChange = {
                            currentCode = it
                            isCopied = false
                        },
                        textStyle = TextStyle(
                            color = AmanixCyanPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(AmanixCyanPrimary),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .testTag("code_editor_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glassmorphic Terminal / Sandbox Output Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .glassmorphic(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0x350A1424),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = AmanixCyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Terminal / Sandbox Output", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmanixTextSecondary)
                        }

                        when (executionResult) {
                            is ExecutionResult.Running -> StatusBadge(status = "RUNNING")
                            is ExecutionResult.NotConfigured -> StatusBadge(status = "NOT CONFIGURED")
                            is ExecutionResult.Error -> StatusBadge(status = "FAILED")
                            is ExecutionResult.Output -> StatusBadge(status = "EXIT 0")
                            else -> StatusBadge(status = "IDLE")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x30000000))
                            .border(0.5.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        when (val res = executionResult) {
                            is ExecutionResult.Idle -> {
                                Text(
                                    text = "$ Terminal ready. Press 'Run' to execute in the secure sandbox.",
                                    color = AmanixTextMuted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            is ExecutionResult.Running -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = AmanixCyanPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Dispatching payload to secure container sandbox...", color = AmanixCyanPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            is ExecutionResult.NotConfigured -> {
                                Column {
                                    Text(
                                        text = "$ STATUS: NOT CONFIGURED",
                                        color = AmanixAccentAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = res.message,
                                        color = AmanixTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            is ExecutionResult.Error -> {
                                Text(
                                    text = "$ ERROR: ${res.message}",
                                    color = AmanixAccentRed,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            is ExecutionResult.Output -> {
                                Column {
                                    if (res.stdout.isNotEmpty()) {
                                        Text(text = res.stdout, color = AmanixTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    if (res.stderr.isNotEmpty()) {
                                        Text(text = res.stderr, color = AmanixAccentRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        text = "[Completed in ${res.executionTimeMs}ms • Exit code: ${res.exitCode}]",
                                        color = AmanixTextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
