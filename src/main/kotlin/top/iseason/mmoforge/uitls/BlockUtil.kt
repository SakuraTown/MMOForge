/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:49
 *
 */

package top.iseason.mmoforge.uitls

import org.bukkit.block.Block
import org.bukkit.util.Vector

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

