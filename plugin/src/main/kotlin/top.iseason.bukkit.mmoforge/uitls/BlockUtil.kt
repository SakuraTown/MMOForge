/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:49
 *
 */

package top.iseason.bukkit.mmoforge.uitls

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector

/**
 * 连锁方块
 * @param start 开始的方块
 * @param types 材质白名单，不设置则与本方块相同
 * @param maxSize 最大数量
 *
 * @return 符合条件的集合
 */
fun getVeinBlocks(start: Block, maxSize: Int = 10, types: Set<Material>? = null): Set<Block> =
    start.getVeinChainBlocks(limit = maxSize, types = types)


/**
 * 获取半径一格内的同类方块,除了自己
 */
fun Block.getNearBlocks(): Set<Block> {
    val totalBlocks = mutableSetOf<Block>()
    relativeVectors.forEach {
        val relative = location.clone().add(it).block
        if (type == relative.type && this != relative) {
            totalBlocks.add(relative)
        }
    }
    return totalBlocks
}

/**
 * 由DFS算法递归获取相似方块
 * @param start 起始方块
 * @param set 将返回的方块集合，不包括起始方块
 * @param limit 限制最大数量
 */
fun Block.getVeinChainBlocks(
    start: Block = this,
    types: Collection<Material>? = null,
    set: MutableSet<Block> = mutableSetOf(),
    limit: Int,
): MutableSet<Block> {
    for (relativeVector in relativeVectors) {
        val relative = location.add(relativeVector).block
        if (set.size >= limit) return set
        if (set.contains(relative) || relative == start) continue
        val rType = relative.type
        if (rType == type || types?.contains(rType) == true) {
            set.add(relative)
            relative.getVeinChainBlocks(start, types, set, limit)
        }
    }
    return set
}

/**
 * 一个由方块组成的 9*9 立方体，其核心方块周围26个方块坐标相对其的偏移矢量
 */
val relativeVectors = arrayOf(
//    Vector(0, 0, 0)
    Vector(1, 0, 0),
    Vector(0, 0, 1),
    Vector(-1, 0, 0),
    Vector(0, -1, 0),
    Vector(0, 0, -1),
    Vector(0, 1, 0),
    Vector(0, 1, 1),
    Vector(1, 1, 0),
    Vector(1, 0, 1),
    Vector(-1, -1, 0),
    Vector(-1, 0, -1),
    Vector(0, -1, -1),
    Vector(1, -1, 0),
    Vector(1, 0, -1),
    Vector(0, 1, -1),
    Vector(-1, 1, 0),
    Vector(-1, 0, 1),
    Vector(0, -1, 1),
    Vector(1, 1, 1),
    Vector(1, 1, -1),
    Vector(1, -1, 1),
    Vector(-1, 1, 1),
    Vector(1, -1, -1),
    Vector(-1, -1, 1),
    Vector(-1, 1, -1),
    Vector(-1, -1, -1),
)
