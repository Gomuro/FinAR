import android.content.Context
import android.graphics.Color as AndroidColor
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color as SceneformColor
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

class Cube(
    context: Context,
    size: Float,
    color: Int,
    transformationSystem: TransformationSystem
) : TransformableNode(transformationSystem) {
    init {
        MaterialFactory.makeOpaqueWithColor(context, SceneformColor(color))
            .thenAccept { material ->
                val cube = ShapeFactory.makeCube(
                    Vector3(size, size, size), 
                    Vector3.zero(), 
                    material
                )
                renderable = cube
            }
    }
} 