package com.netsound.shield

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsound.shield.services.GameVpnService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetSoundShieldTheme {
                MainScreen(
                    onRequestVpn = { checkAndStartVpn() },
                    onStopVpn = { stopVpnService() }
                )
            }
        }
    }

    private fun checkAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, GameVpnService::class.java)
        startService(intent)
    }

    private fun stopVpnService() {
        val intent = Intent(this, GameVpnService::class.java).apply {
            action = "STOP"
        }
        startService(intent)
    }
}

@Composable
fun MainScreen(onRequestVpn: () -> Unit, onStopVpn: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isMasterActive by remember { mutableStateOf(false) }
    var accentColor by remember { mutableStateOf(Color(0xFF00E5FF)) }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    var freeRamMb by remember { mutableLongStateOf(getFreeMemory(context)) }

    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "GlowAlpha"
    )

    LaunchedEffect(isMasterActive) {
        if (isMasterActive) {
            while (true) {
                delay(1000)
                sessionSeconds++
            }
        } else {
            sessionSeconds = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0C1017), Color(0xFF030406)),
                    radius = 1400f
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("NetSound", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("PRO GAMING BOOSTER", color = Color.Gray, fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 16.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12161F).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isMasterActive) "РЕЖИМ АКТИВЕН" else "РЕЖИМ ВЫКЛЮЧЕН",
                        color = if (isMasterActive) accentColor else Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(
                                elevation = if (isMasterActive) (20 * glowAlpha).dp else 0.dp,
                                shape = CircleShape,
                                spotColor = accentColor
                            )
                            .clip(CircleShape)
                            .background(if (isMasterActive) accentColor else Color(0xFF1A202C))
                            .border(2.dp, if (isMasterActive) Color.White else Color.Gray.copy(alpha = 0.3f), CircleShape)
                            .clickable {
                                isMasterActive = !isMasterActive
                                playSoundEffect(context)
                                if (isMasterActive) {
                                    onRequestVpn()
                                    launchPubgMobile(context)
                                } else {
                                    onStopVpn()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isMasterActive) "⚡" else "POWER",
                                color = if (isMasterActive) Color.Black else Color.Gray,
                                fontSize = 28.sp
                            )
                            Text(
                                text = if (isMasterActive) "ONLINE" else "START",
                                color = if (isMasterActive) Color.Black else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isMasterActive) "Защита сети и оптимизация включены" else "Нажмите для запуска оптимизатора и игры",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TelemetryWidget(
                    modifier = Modifier.weight(1f),
                    title = "ПРОЦЕССОР",
                    value = "${getCpuTemperature()} °C",
                    icon = "🔥"
                )
                TelemetryWidget(
                    modifier = Modifier.weight(1f),
                    title = "СЕАНС",
                    value = formatSeconds(sessionSeconds),
                    icon = "⏱️"
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12161F).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Очистка RAM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Свободно: ${freeRamMb} MB", color = Color.Gray, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                System.gc()
                                delay(400)
                                freeRamMb = getFreeMemory(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f))
                    ) {
                        Text("ОЧИСТИТЬ", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryWidget(modifier: Modifier, title: String, value: String, icon: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12161F).copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 9.sp)
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

fun playSoundEffect(context: Context) {
    try {
        val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun launchPubgMobile(context: Context) {
    val pubgPackages = listOf("com.tencent.ig", "com.pubg.krmobile", "com.vng.pubgmobile")
    val pm = context.packageManager
    for (pkg in pubgPackages) {
        val intent = pm.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
    }
}

fun getFreeMemory(context: Context): Long {
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)
    return memInfo.availMem / (1024 * 1024)
}

fun getCpuTemperature(): Float {
    return try {
        val thermalFile = File("/sys/class/thermal/thermal_zone0/temp")
        if (thermalFile.exists()) {
            val temp = thermalFile.readText().trim().toFloat()
            if (temp > 1000) temp / 1000f else temp
        } else 36.5f
    } catch (e: Exception) {
        37.0f
    }
}

fun formatSeconds(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
}

@Composable
fun NetSoundShieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(background = Color(0xFF06070A)),
        content = content
    )
}
