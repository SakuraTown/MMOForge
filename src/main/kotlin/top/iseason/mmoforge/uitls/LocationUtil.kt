/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/13 下午2:04
 *
 */

package top.iseason.mmoforge.uitls

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

fun Location.getNormalX(): Vector {
    val vector = Vector()
    val rotX = yaw.toDouble()
    //row =0 , pitch = 0
    vector.x = cos(Math.toRadians(rotX))
    vector.z = sin(Math.toRadians(rotX))
    return vector
}

fun Location.getNormalZ(): Vector {
    val vector = Vector()
    val rotX = yaw.toDouble()
    val rotY = pitch.toDouble()
    // row = 0
    vector.y = -sin(Math.toRadians(rotY))
    val xz = cos(Math.toRadians(rotY))
    vector.x = -xz * sin(Math.toRadians(rotX))
    vector.z = xz * cos(Math.toRadians(rotX))
    return vector
}

// 以自身为原点和相对坐标系获取世界坐标
fun Location.getRelative(
    coordinate: Array<Vector>,
    x: Double,
    y: Double,
    z: Double
): Location {
    return clone().apply {
        add(coordinate[0].clone().multiply(x))
        add(coordinate[1].clone().multiply(y))
        add(coordinate[2].clone().multiply(z))
    }
}

/**
 * 由变换矩阵求出变换后的位置
 */
fun Location.transform(transformMatrix: Matrix<Double>): Location {
    val pointMatrix = matrixOf(
        1, 4,
        x, y, z, 1.0
    )
    val matrix = transformMatrix dot pointMatrix
    return Location(world, matrix[0][0], matrix[1][0], matrix[2][0])
}
