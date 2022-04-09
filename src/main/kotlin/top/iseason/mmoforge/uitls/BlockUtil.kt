/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:49
 *
 */

package top.iseason.mmoforge.uitls

import org.bukkit.block.Block
import org.bukkit.util.Vector

/**
 * 连锁相同的方块
 * @param start 开始的方块
 * @param maxSize 最大数量
 */
fun getVeinBlocks(start: Block, maxSize: Int): Set<Block> = start.getVeinChainBlocks(limit = maxSize)

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
    set: MutableSet<Block> = mutableSetOf(),
    limit: Int,
): MutableSet<Block> {
    for (relativeVector in relativeVectors) {
        val relative = location.clone().add(relativeVector).block
        if (set.size >= limit) return set
        if (type == relative.type && !set.contains(relative) && relative != start) {
            set.add(relative)
            relative.getVeinChainBlocks(start, set, limit)
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
