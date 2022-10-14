package com.joshafeinberg.babyblocker

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joshafeinberg.babyblocker.ui.theme.BabyBlockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyBlockerTheme {
                // A surface container using the 'background' color from the theme
                val intent = Intent(this, NotificationService::class.java)

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {

                    Column(Modifier.padding(16.dp)) {

                        var hasOverlayPermission by remember { mutableStateOf(hasOverlayPermission(this@MainActivity)) }
                        var openDialog by remember { mutableStateOf(false) }
                        val isNotificationActive by BabyBlockerStatus.notificationActive.collectAsState(isServiceRunning())
                        val isBlockerRunning by BabyBlockerStatus.babyBlockerStatus.collectAsState()

                        Text(stringResource(R.string.welcome), style = MaterialTheme.typography.h2, textAlign = TextAlign.Center)

                        if (!hasOverlayPermission) {
                            Row {
                                SystemOverlayButton(modifier = Modifier) {
                                    if (hasOverlayPermission(this@MainActivity)) {
                                        hasOverlayPermission = true
                                    } else {
                                        openDialog = true
                                    }
                                }

                                IconButton(onClick = { openDialog = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_question_mark_24),
                                        contentDescription = "?"
                                    )
                                }
                            }

                            if (openDialog) {
                                SystemOverlayExplainer(onDismissRequest = { openDialog = false })
                            }
                        } else if (!isNotificationActive) {
                            Button(onClick = {
                                applicationContext.startForegroundService(intent)
                            }) {
                                Text(stringResource(R.string.start))
                            }
                        } else {
                            Button(onClick = {
                                applicationContext.stopService(intent)
                            }) {
                                Text(stringResource(R.string.stop))
                            }

                            if (isBlockerRunning) {
                                Text(stringResource(R.string.blocking_on_header), color = MaterialTheme.colors.error, style = MaterialTheme.typography.h4, textAlign = TextAlign.Center)
                                Text(stringResource(R.string.blocking_on_body), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemOverlayButton(
    modifier: Modifier,
    onPermissionRequestReturn: () -> Unit
) {
    val overlayPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            onPermissionRequestReturn()
        })

    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${
        LocalContext.current.packageName}"))
    
    Button(modifier = modifier, onClick = {
        overlayPermissionRequest.launch(intent)
    }) {
        Text(stringResource(R.string.system_overlay_button))
    }
}

@Composable
fun SystemOverlayExplainer(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.system_overlay_dialog_title))
        },
        text = {
            Text(stringResource(R.string.system_overlay_dialog_text))
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

private fun hasOverlayPermission(context: Context) = Settings.canDrawOverlays(context)

@Suppress("DEPRECATION")
fun Context.isServiceRunning(): Boolean {
    return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == NotificationService::class.java.name }
}
