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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.joshafeinberg.babyblocker.ui.theme.BabyBlockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyBlockerTheme {
                // A surface container using the 'background' color from the theme
                val intent = Intent(this, NotificationService::class.java)

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {

                    var hasOverlayPermission by remember { mutableStateOf(hasOverlayPermission(this)) }
                    var openDialog by remember { mutableStateOf(false) }
                    val isServiceRunning by BabyBlockerStatus.babyBlockerStatus.collectAsState()

                    if (!hasOverlayPermission) {
                        SystemOverlayButton(modifier = Modifier) {
                            if (hasOverlayPermission(this)) {
                                hasOverlayPermission = true
                            } else {
                                openDialog = true
                            }
                        }

                        if (openDialog) {
                            SystemOverlayExplainer(onDismissRequest = { openDialog = false})
                        }
                    } else if (!isServiceRunning) {
                        Button(onClick = {
                            applicationContext.startForegroundService(intent)
                        }) {
                            Text("Start BabyBlocker")
                        }
                    } else {
                        Button(onClick = {
                            applicationContext.stopService(intent)
                        }) {
                            Text("Stop BabyBlocker")
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
        Text("Enable System Overlay")
    }
}

@Composable
fun SystemOverlayExplainer(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Why we need system overlay permission")
        },
        text = {
            Text("We need this permission to allow us to draw a blank screen over your app to block clicks")
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
