/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午10:24
 *
 */

package top.iseason.mmoforge.uitls

import java.text.DecimalFormat

fun getProcessBar(size: Int, current: Double, max: Double): String {
    val fillCount = ((current / max) * size).toInt()
    val format = DecimalFormat("0.##")
    return "|${"█".repeat(fillCount)}${" ".repeat(size - fillCount)}| ${format.format(current)}/${format.format(max)}"
}

fun getStarCount(star: Int): String {
    return "✪ ".repeat(star)
}

inline fun <T, reified U> List<T>.toType(): List<U> {
    val list = mutableListOf<U>()
    forEach {
        if (it is U)
            list.add(it)
    }
    return list
}