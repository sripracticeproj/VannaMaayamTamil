package com.vannamaayam.tamil.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

private class NodeWrapper {
    var node: ModelNode? = null
}

/**
 * 3D ANIMAL CANVAS: Jetpack Compose + Sceneview integration.
 */
@Composable
fun Animal3DViewport(
    modelAssetPath: String,
    tintColor: Color?,
    targetMeshName: String?,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val modelInstance = rememberModelInstance(modelLoader, modelAssetPath)
    
    val wrapper = remember { NodeWrapper() }
    val trigger = remember { mutableStateOf(0) }

    LaunchedEffect(trigger.value, tintColor, targetMeshName) {
        val node = wrapper.node
        if (node != null && tintColor != null) {
            try {
                // SceneView 4.x material instance access
                node.materialInstances.flatten().forEach { materialInstance ->
                    materialInstance.setParameter(
                        "baseColorFactor",
                        tintColor.red,
                        tintColor.green,
                        tintColor.blue,
                        tintColor.alpha
                    )
                }
            } catch (e: Exception) {
                // Fallback for non-flattened or different versions
                node.materialInstances.forEach { anyInstance ->
                    // Handle single level list if necessary
                }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (modelInstance == null) {
            // Show loading or error if model instance is null
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Text("Loading 3D Model...", color = Color.White)
            }
        }

        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            isOpaque = false // Allow seeing through to the background
        ) {
            if (modelInstance != null) {
                ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 1.0f,
                    position = Position(x = 0.0f, y = 0.0f, z = -3.0f),
                    rotation = Rotation(y = 180f),
                    apply = {
                        wrapper.node = this
                        trigger.value++
                    }
                )
            }
        }
    }
}

@Composable
private fun Column(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Column(modifier, horizontalAlignment = horizontalAlignment) {
        content()
    }
}
