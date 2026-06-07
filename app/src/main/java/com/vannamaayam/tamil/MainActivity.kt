package com.vannamaayam.tamil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.vannamaayam.tamil.speech.TamilSpeechManager
import com.vannamaayam.tamil.ui.VannaMaayamDashboard
import com.vannamaayam.tamil.ui.theme.VannaMaayamTamilTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val speechManager = remember { TamilSpeechManager(context) }

            val hasRecordPermission = remember { 
                mutableStateOf(speechManager.hasRecordAudioPermission()) 
            }
            
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasRecordPermission.value = isGranted
            }

            VannaMaayamTamilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VannaMaayamDashboard(
                        hasRecordPermission = hasRecordPermission.value,
                        onRequestPermission = {
                            launcher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }
            }
        }
    }
}
