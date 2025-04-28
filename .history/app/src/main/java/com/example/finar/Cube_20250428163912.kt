import android.content.Context
import android.graphics.Color
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.TransformableNode

class Cube(context: Context, size: Float, color: android.graphics.Color) : TransformableNode() {
    init {
        MaterialFactory.makeOpaqueWithColor(context, com.google.ar.sceneform.rendering.Color(color))
            .thenAccept { material ->
                val cube = ShapeFactory.makeCube(Vector3(size, size, size), Vector3.zero(), material)
                renderable = cube
            }
    }
} 