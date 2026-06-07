package com.vannamaayam.tamil.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

private class NodeWrapper {
    var node: io.github.sceneview.node.ModelNode? = null
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
            // Flatten for SceneView 4.18.0 as it returns List<List<MaterialInstance>>
            node.materialInstances.flatten().forEach { materialInstance ->
                materialInstance.setParameter(
                    "baseColorFactor",
                    tintColor.red,
                    tintColor.green,
                    tintColor.blue,
                    tintColor.alpha
                )
            }
        }
    }

    SceneView(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader
    ) {
        val instance = modelInstance
        if (instance != null) {
            ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.0f,
                position = Position(z = -3.0f),
                apply = {
                    // Using 'this' as it is the receiver for the 'apply' block in ModelNode
                    wrapper.node = this
                    trigger.value++
                }
            )
        }
    }
}
