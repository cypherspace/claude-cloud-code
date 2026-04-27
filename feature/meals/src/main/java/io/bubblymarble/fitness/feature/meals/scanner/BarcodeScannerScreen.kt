package io.bubblymarble.fitness.feature.meals.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun BarcodeScannerScreen(
    onScanned: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Latch so we only emit one barcode per session even if ML Kit detects the same frame twice.
    var emitted by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when {
            permissionGranted -> CameraSurface(
                onScanned = { value ->
                    if (!emitted) {
                        emitted = true
                        onScanned(value)
                    }
                },
            )
            permissionDenied -> PermissionDeniedBlock(
                onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onCancel = onCancel,
            )
        }

        ScannerOverlay(onCancel = onCancel)
    }
}

@Composable
private fun CameraSurface(onScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { BarcodeAnalysisController() }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(controller) {
        onDispose { controller.shutdown() }
    }

    LaunchedEffect(controller, previewView) {
        controller.bind(context, lifecycleOwner, previewView, onScanned)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { previewView },
    )
}

@Composable
private fun ScannerOverlay(onCancel: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Point at a barcode",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            "EAN-8/13, UPC-A/E, Code 128",
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Cancel", color = Color.White)
        }
    }
}

@Composable
private fun BoxScope.PermissionDeniedBlock(onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier.align(Alignment.Center).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Camera permission needed to scan barcodes.",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Grant access") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onCancel) { Text("Cancel", color = Color.White) }
    }
}
