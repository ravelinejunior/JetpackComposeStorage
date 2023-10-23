package com.raveline.concord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.raveline.concord.extensions.showLog
import com.raveline.concord.navigation.ConcordNavHost
import com.raveline.concord.ui.theme.ConcordTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filesDir.listFiles()?.forEach { file ->
            showLog(TAG, "File: ${file.name}")
            showLog(TAG, "Path: ${file.path}")
        }

        getExternalFilesDir(null)?.listFiles()?.forEach { file ->
            showLog(TAG, "External File: ${file.name}")
            showLog(TAG, "External File Path: ${file.path}")
        }

        setContent {
            ConcordTheme {
                val navController = rememberNavController()
                ConcordNavHost(navController = navController)
            }
        }
    }
}


