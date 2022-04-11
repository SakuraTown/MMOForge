/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/24 下午11:35
 *
 */

package top.iseason.mmoforge.uitls

import com.entiv.core.hook.VaultEconomyHook
import com.entiv.core.utils.sendErrorMessage
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.cos
import kotlin.math.sin

/**
 * 扣除玩家一定金额
 * @param money 金额
 * @return true 为成功 false 为失败
 */
fun Player.takeMoney(money: Double): Boolean {
    if (VaultEconomyHook.has(this, money) == true) {
        val response = VaultEconomyHook.withdraw(this, money)
        if (response?.type != EconomyResponse.ResponseType.SUCCESS) {
            sendErrorMessage(this, "余额不足!")
            return false
        }
    } else {
        sendErrorMessage(this, "余额不足!")
        return false
    }
    return true
}

/**
 * 在方块处掉落该方块相应的掉落物(使用指定工具)
 */
fun Player.dropBlock(block: Block, tool: ItemStack?) {
    val drops = block.getDrops(tool)
    val itemList = mutableListOf<Item>()
    for (drop in drops) {
        itemList.add(dropItemNaturally(block.location, drop))
    }
    val blockDropItemEvent = BlockDropItemEvent(block, block.state, this, itemList)
    Bukkit.getServer().pluginManager.callEvent(blockDropItemEvent)
    if (blockDropItemEvent.isCancelled) {
        itemList.forEach { it.remove() }
    }
}

/**
 * 在给定方块周围获取与玩家视角平行的 xyz 矩形区域内的方块
 * @param target 起始方块
 * @param rangeX 宽度
 * @param rangeY 高度
 * @param rangeZ 深度
 * @return 所有方块的集合(除了 target)
 */
fun Player.getScopeBlocks(target: Block, rangeX: Int, rangeY: Int, rangeZ: Int): Set<Block> {
    val set = mutableSetOf<Block>()
    val location = target.location
    val world = location.world
    // X轴旋转角(角度 转 弧度)
    val eyePitch = eyeLocation.pitch / 180.0 * Math.PI
    //Y轴旋转角(角度 转 弧度)
    val eyeYaw = eyeLocation.yaw / 180.0 * Math.PI
    //生成变换矩阵
    val transformMatrix =
        getTransformMatrix(
            location.x + 0.5,
            location.y + 0.5,
            location.z + 0.5,
            eyePitch,
            -eyeYaw,
            0.0
        )
    val halfRangeX = rangeX / 2
    val halfRangeY = rangeY / 2
    //长
    for (x in -halfRangeX until rangeX - halfRangeX) {
        //宽
        for (y in -halfRangeY until rangeY - halfRangeY) {
            //深
            for (z in 0 until rangeZ) {
                val block = Location(world, x.toDouble(), y.toDouble(), z.toDouble()).transform(transformMatrix).block
                //跳过水
                if (block.isLiquid) continue
                //跳过不可挖掘方块
                if (block.getBreakSpeed(this) == 0.0F) continue
                set.add(block)
            }
        }
    }
    set.remove(target)
    return set
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

/**
 * 根据变换生成变换矩阵，支持平移和旋转
 */
fun getTransformMatrix(
    moveX: Double,
    moveY: Double,
    moveZ: Double,
    angleX: Double,
    angleY: Double,
    angleZ: Double
): Matrix<Double> {
    //平移矩阵
    val translationMatrix = matrixOf(
        4, 4,
        1.0, 0.0, 0.0, moveX,
        0.0, 1.0, 0.0, moveY,
        0.0, 0.0, 1.0, moveZ,
        0.0, 0.0, 0.0, 1.0
    )
    // X 轴旋转矩阵
    val rotationMatrixX = matrixOf(
        4, 4,
        1.0, 0.0, 0.0, 0.0,
        0.0, cos(angleX), -sin(angleX), 0.0,
        0.0, sin(angleX), cos(angleX), 0.0,
        0.0, 0.0, 0.0, 1.0
    )
    // Y 轴旋转矩阵
    val rotationMatrixY = matrixOf(
        4, 4,
        cos(angleY), 0.0, sin(angleY), 0.0,
        0.0, 1.0, 0.0, 0.0,
        -sin(angleY), 0.0, cos(angleY), 0.0,
        0.0, 0.0, 0.0, 1.0
    )
    // Z 轴旋转矩阵
    val rotationMatrixZ = matrixOf(
        4, 4,
        cos(angleZ), -sin(angleZ), 0.0, 0.0,
        sin(angleZ), cos(angleZ), 0.0, 0.0,
        0.0, 0.0, 1, 0.0,
        0.0, 0.0, 0.0, 1.0
    )
    val rotationMatrix = rotationMatrixY dot rotationMatrixX dot rotationMatrixZ

    return translationMatrix dot rotationMatrix
}
