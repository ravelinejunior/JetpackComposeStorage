package com.raveline.concord.security

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raveline.concord.ui.theme.ConcordTheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.M)
class SecurityAppActivity : ComponentActivity() {

    private val cryptoDecryptManager = KeyEncryptDecryptManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cryptoDecryptManager.generateAndStoreCertificate(this)

        setContent {
            ConcordTheme {
                var messageToEncrypt by remember {
                    mutableStateOf("")
                }

                var messageToDecrypt by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),

                    ) {
                    TextField(
                        value = messageToEncrypt,
                        onValueChange = {
                            messageToEncrypt = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Encrypt String")
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.align(CenterHorizontally)
                    ) {
                        Button(onClick = {
                            val bytes = messageToEncrypt.encodeToByteArray()
                            val file = File(filesDir, "secrets.txt")
                            if (!file.exists()) {
                                file.createNewFile()
                            }
                            val fos = FileOutputStream(file)

                            messageToDecrypt = cryptoDecryptManager.encrypt(
                                byteArray = bytes,
                                outputStream = fos
                            ).decodeToString()


                        }) {
                            Text(text = "Encrypt")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = {
                            val file = File(filesDir, "secrets.txt")
                            messageToEncrypt = cryptoDecryptManager.decrypt(
                                inputStream = FileInputStream(file)
                            ).decodeToString()

                        }) {
                            Text(text = "Decrypt")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = messageToDecrypt)
                }
            }
        }
    }


}