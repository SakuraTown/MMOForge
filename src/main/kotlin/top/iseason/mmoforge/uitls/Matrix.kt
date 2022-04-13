/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/12 上午12:03
 *
 */

package top.iseason.mmoforge.uitls

import kotlin.math.cos
import kotlin.math.sin

interface Matrix<out T> {
    val cols: Int
    val rows: Int

    fun getRow(index: Int): List<T> = filterIndexed { _, row, _ -> row == index }
    fun getColumn(index: Int): List<T> = filterIndexed { col, _, _ -> col == index }

    operator fun get(x: Int, y: Int): T
    operator fun get(row: Int): List<T> = getRow(row)
}

val <T> Matrix<T>.size: Int
    get() = this.cols * this.rows

interface MutableMatrix<T> : Matrix<T> {
    operator fun set(x: Int, y: Int, value: T)
}

abstract class AbstractMatrix<out T> : Matrix<T> {

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        forEachIndexed { x, y, value ->
            if (x == 0)
                sb.append('[')
            sb.append(value.toString())
            if (x == cols - 1) {
                sb.append(']')
                if (y < rows - 1)
                    sb.append(", ")
            } else {
                sb.append(", ")
            }
        }
        sb.append(']')
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Matrix<*>) return false
        if (rows != other.rows || cols != other.cols) return false

        var eq = true
        forEachIndexed { x, y, value ->
            if (value === null) {
                if (other[x, y] !== null) {
                    eq = false
                    return@forEachIndexed
                }
            } else {
                if (value != other[x, y]) {
                    eq = false
                    return@forEachIndexed
                }
            }
        }
        return eq
    }

    override fun hashCode(): Int {
        var h = 17
        h = h * 39 + cols
        h = h * 39 + rows
        forEach { h = h * 37 + (it?.hashCode() ?: 1) }
        return h
    }
}

internal open class TransposedMatrix<out T>(protected val original: Matrix<T>) : AbstractMatrix<T>() {
    override val cols: Int
        get() = original.rows

    override val rows: Int
        get() = original.cols

    override fun get(x: Int, y: Int): T = original[y, x]
}

internal class TransposedMutableMatrix<T>(original: MutableMatrix<T>) : TransposedMatrix<T>(original),
    MutableMatrix<T> {
    override fun set(x: Int, y: Int, value: T) {
        (original as MutableMatrix<T>)[y, x] = value
    }
}

fun <T> Matrix<T>.asTransposed(): Matrix<T> = TransposedMatrix(this)

fun <T> MutableMatrix<T>.asTransposed(): MutableMatrix<T> = TransposedMutableMatrix(this)

internal open class ListMatrix<out T>(
    override val cols: Int,
    override val rows: Int,
    protected val list: List<T>
) : AbstractMatrix<T>() {
    override operator fun get(x: Int, y: Int): T = list[y * cols + x]
}

internal class MutableListMatrix<T>(
    cols: Int, rows: Int,
    list: MutableList<T>
) : ListMatrix<T>(cols, rows, list), MutableMatrix<T> {
    override fun set(x: Int, y: Int, value: T) {
        (list as MutableList<T>)[y * cols + x] = value
    }
}

fun <T> matrixOf(cols: Int, rows: Int, vararg elements: T): Matrix<T> = ListMatrix(cols, rows, elements.asList())

fun <T> mutableMatrixOf(cols: Int, rows: Int, vararg elements: T): MutableMatrix<T> =
    MutableListMatrix(cols, rows, elements.toMutableList())

private inline fun <T> prepareListForMatrix(cols: Int, rows: Int, init: (Int, Int) -> T): ArrayList<T> {
    val list = ArrayList<T>(cols * rows)
    for (y in 0 until rows) {
        for (x in 0 until cols) {
            list.add(init(x, y))
        }
    }
    return list
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <T> createMatrix(cols: Int, rows: Int, init: (Int, Int) -> T): Matrix<T> =
    ListMatrix(cols, rows, prepareListForMatrix(cols, rows, init))

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <T> createMutableMatrix(cols: Int, rows: Int, init: (Int, Int) -> T): MutableMatrix<T> =
    MutableListMatrix(cols, rows, prepareListForMatrix(cols, rows, init))

inline fun <T, U> Matrix<T>.mapIndexed(transform: (Int, Int, T) -> U): Matrix<U> =
    createMatrix(cols, rows) { x, y -> transform(x, y, this[x, y]) }

inline fun <T, U> Matrix<T>.map(transform: (T) -> U): Matrix<U> = mapIndexed { _, _, value -> transform(value) }

inline fun <T> Matrix<T>.filterIndexed(predicate: (Int, Int, T) -> Boolean): List<T> {
    val list = mutableListOf<T>()
    forEachIndexed { x, y, value ->
        if (predicate(x, y, value)) list.add(value)
    }
    return list
}

inline fun <T> Matrix<T>.forEachIndexed(action: (Int, Int, T) -> Unit) {
    for (y in 0 until rows) {
        for (x in 0 until cols) {
            action(x, y, this[x, y])
        }
    }
}

inline fun <T> Matrix<T>.forEach(action: (T) -> Unit) = forEachIndexed { _, _, value -> action(value) }

fun <T> Matrix<T>.toList(): List<T> = prepareListForMatrix(cols, rows) { x, y -> this[x, y] }

fun <T> Matrix<T>.toMutableList(): MutableList<T> = prepareListForMatrix(cols, rows) { x, y -> this[x, y] }

private fun <T> Iterable<T>.toArrayList(size: Int): ArrayList<T> {
    val list = ArrayList<T>(size)
    val itr = iterator()

    for (i in 0 until size) {
        if (itr.hasNext()) {
            list.add(itr.next())
        } else {
            throw IllegalArgumentException("No enough elements")
        }
    }
    return list
}

fun <T> Iterable<T>.toMatrix(cols: Int, rows: Int): Matrix<T> {
    val list = toArrayList(cols * rows)
    return ListMatrix(cols, rows, list)
}

fun <T> Iterable<T>.toMutableMatrix(cols: Int, rows: Int): MutableMatrix<T> {
    val list = toArrayList(cols * rows)
    return MutableListMatrix(cols, rows, list)
}

operator fun <M : Number, N : Number> Matrix<M>.plus(other: Matrix<N>): Matrix<Double> {
    if (rows != other.rows || cols != other.cols)
        throw IllegalArgumentException("The matrices do not match")

    return mapIndexed { x, y, value -> value.toDouble() + other[x, y].toDouble() }
}

operator fun <N : Number> Matrix<N>.unaryMinus(): Matrix<Double> = map { -it.toDouble() }

operator fun <M : Number, N : Number> Matrix<M>.minus(other: Matrix<N>): Matrix<Double> = this + (-other)

operator fun <M : Number, N : Number> Matrix<M>.times(other: Matrix<N>): Matrix<Double> {
    if (rows != other.rows || cols != other.cols)
        throw IllegalArgumentException("The matrices do not match")

    return mapIndexed { x, y, value -> value.toDouble() * other[x, y].toDouble() }
}

operator fun <M : Number> Matrix<M>.times(other: Number): Matrix<Double> = map { it.toDouble() * other.toDouble() }

operator fun <M : Number> Number.times(other: Matrix<M>): Matrix<Double> = other * this

operator fun <M : Number, N : Number> Matrix<M>.div(other: Matrix<N>): Matrix<Double> {
    if (rows != other.rows || cols != other.cols)
        throw IllegalArgumentException("The matrices do not match")

    return mapIndexed { x, y, value -> value.toDouble() / other[x, y].toDouble() }
}

operator fun <M : Number> Matrix<M>.div(other: Number): Matrix<Double> = map { it.toDouble() / other.toDouble() }

@Deprecated(message = "This function will be removed in a future version", replaceWith = ReplaceWith("dot"))
infix fun <M : Number, N : Number> Matrix<M>.x(other: Matrix<N>): Matrix<Double> {
    if (rows != other.cols)
        throw IllegalArgumentException("The matrices do not match")

    return createMatrix(cols, other.rows) { x, y ->
        var value = .0
        for (i in 0 until rows)
            value += this[x, i].toDouble() * other[i, y].toDouble()
        value
    }
}

infix fun <M : Number, N : Number> Matrix<M>.dot(other: Matrix<N>): Matrix<Double> {
    if (cols != other.rows)
        throw IllegalArgumentException("The matrices do not match: this has $cols columns, other has ${other.rows} rows")

    return createMatrix(other.cols, rows) { y, x ->
        var value = 0.0
        for (i in 0 until cols)
            value += this[i, x].toDouble() * other[y, i].toDouble()
        value
    }
}

fun <M : Number> Matrix<M>.determinant(): Double = when {
    rows != cols -> throw IllegalArgumentException("Cannot compute the determinant for non-squared matrices")
    rows == 1 -> this[0, 0].toDouble()
    rows == 2 -> this[0, 0].toDouble() * this[1, 1].toDouble() - (this[1, 0].toDouble() * this[0, 1].toDouble())
    else -> {
        var mvalue = 0.0
        var negative = false
        for (column in 0 until cols) {
            val posValue = this[column, 0].toDouble() * determinantOfSubMatrix(0, column)
            mvalue += if (negative) -posValue else posValue
            negative = !negative
        }
        mvalue
    }
}

private fun <M : Number> Matrix<M>.determinantOfSubMatrix(row: Int, col: Int): Double {
    val list: MutableList<Double> = mutableListOf()
    mapIndexed { x, y, value ->
        if (x != col && y != row) list.add(value.toDouble())
        value
    }
    return matrixOf(cols - 1, rows - 1, *list.toTypedArray()).determinant()
}

/**
 * 根据变换生成变换矩阵，支持平移、旋转 和缩放
 */
fun getTransformMatrix(
    moveX: Double = 0.0,
    moveY: Double = 0.0,
    moveZ: Double = 0.0,
    angleX: Double = 0.0,
    angleY: Double = 0.0,
    angleZ: Double = 0.0,
    scaleX: Double = 1.0,
    scaleY: Double = 1.0,
    scaleZ: Double = 1.0,
): Matrix<Double> {
//    //平移矩阵
//    val translationMatrix = matrixOf(
//        4, 4,
//        1.0, 0.0, 0.0, moveX,
//        0.0, 1.0, 0.0, moveY,
//        0.0, 0.0, 1.0, moveZ,
//        0.0, 0.0, 0.0, 1.0
//    )
//    // X 轴旋转矩阵
//    val rotationMatrixX = matrixOf(
//        4, 4,
//        1.0, 0.0, 0.0, 0.0,
//        0.0, cos(angleX), -sin(angleX), 0.0,
//        0.0, sin(angleX), cos(angleX), 0.0,
//        0.0, 0.0, 0.0, 1.0
//    )
//    // Y 轴旋转矩阵
//    val rotationMatrixY = matrixOf(
//        4, 4,
//        cos(angleY), 0.0, sin(angleY), 0.0,
//        0.0, 1.0, 0.0, 0.0,
//        -sin(angleY), 0.0, cos(angleY), 0.0,
//        0.0, 0.0, 0.0, 1.0
//    )
//    // Z 轴旋转矩阵
//    val rotationMatrixZ = matrixOf(
//        4, 4,
//        cos(angleZ), -sin(angleZ), 0.0, 0.0,
//        sin(angleZ), cos(angleZ), 0.0, 0.0,
//        0.0, 0.0, 1, 0.0,
//        0.0, 0.0, 0.0, 1.0
//    )
//    //缩放矩阵
//    val scalingMatrix = matrixOf(
//        4, 4,
//        scaleX, 0.0, 0.0, 0.0,
//        0.0, scaleY, 0.0, 0.0,
//        0.0, 0.0, scaleZ, 0.0,
//        0.0, 0.0, 0.0, 1.0
//    )
//    val rotationMatrix = rotationMatrixY dot rotationMatrixX dot rotationMatrixZ
//    val transform = translationMatrix dot rotationMatrix dot scalingMatrix
//    return transform

    //一个等于上面这些
    return matrixOf(
        4,
        4,
        scaleX * cos(angleY) * cos(angleZ),
        scaleY * (sin(angleX) * sin(angleY) * cos(angleZ) - cos(angleX) * sin(angleZ)),
        scaleZ * (cos(angleX) * sin(angleY) * cos(angleZ) + cos(angleX) * sin(angleZ)),
        moveX,

        scaleX * cos(angleX) * sin(angleZ),
        scaleY * (sin(angleX) * sin(angleY) * sin(angleZ) + cos(angleX) * cos(angleZ)),
        scaleZ * (cos(angleX) * sin(angleY) * sin(angleZ) - sin(angleX) * cos(angleZ)),
        moveY,

        -scaleX * sin(angleY),
        scaleY * sin(angleX) * cos(angleY),
        scaleZ * cos(angleX) * cos(angleY),
        moveZ,

        0.0, 0.0, 0.0, 1.0
    )
}
