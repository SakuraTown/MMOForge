/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午10:24
 *
 */

package top.iseason.bukkit.mmoforge.uitls

import top.iseason.bukkit.mmoforge.config.MainConfig

fun getProcessBar(size: Int, current: Double, max: Double): String {
    val fillCount = ((current / max) * size).toInt()
    return "${MainConfig.forgeProgressChar.repeat(fillCount)}${" ".repeat(size - fillCount)}"
}

fun getStarCount(star: Int): String {
    return MainConfig.starChar.repeat(star)
}

inline fun <T, reified U> List<T>.toType(): List<U> {
    val list = mutableListOf<U>()
    forEach {
        if (it is U)
            list.add(it)
    }
    return list
}