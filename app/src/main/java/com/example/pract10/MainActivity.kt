package com.example.pract10

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pract10.ui.theme.Pract10Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pract10Theme {
                ImageDownloadApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pract10Theme {
        ImageDownloadApp()
    }
}

@Composable
fun ImageDownloadApp() {
    var url by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Enter Image URL") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (url.isNotEmpty()) {
                coroutineScope.launch {
                    val downloadedBitmap = downloadImage(url, context)
                    bitmap = downloadedBitmap
                }
            } else {
                Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        },
            modifier = Modifier.fillMaxWidth()) {
            Text("Download Image")
        }
        Spacer(modifier = Modifier.height(16.dp))
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(400.dp),
                alignment = Alignment.Center
                )
        }
    }
}

suspend fun downloadImage(url: String, context:Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val bitmap = downloadImageFromNetwork(url)

            if (bitmap != null) {
                saveImageToDisk(bitmap, context)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image downloaded successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private fun downloadImageFromNetwork(url: String): Bitmap? {
    return try {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val inputStream: InputStream = response.body?.byteStream() ?: return null
            BitmapFactory.decodeStream(inputStream)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveImageToDisk(bitmap: Bitmap, context: Context) {
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "downloadedImages")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val file = File(directory, "downloaded_image.png")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
}