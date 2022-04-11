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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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

fun Player.getScopeBlocks(target: Block, rangeX: Int, rangeY: Int): Set<Block> {
    val set = mutableSetOf<Block>()
    val location = target.location
    println("start ============")
//    println("target $location")
    val world = location.world
    val eyePitch = eyeLocation.pitch
    val eyeYaw = eyeLocation.yaw
    val pitch = (eyePitch / 45).roundToInt() * 45.0
    val yaw = (eyeYaw / 45).roundToInt() * 45.0

//    // 180.0 * Math.PI
//    println("eyePitch = $eyePitch")
//    println("eyeYaw = $eyeYaw")
//    println("pitch = $pitch")
//    println("yaw = $yaw")
    val transformMatrix = getTransformMatrix(target.location, pitch / 180.0 * Math.PI, yaw / 180.0 * Math.PI, 0.0)
    val halfRangeX = rangeX / 2
    val halfRangeY = rangeY / 2
    for (x in -halfRangeX until rangeX - halfRangeX) {
        for (y in -halfRangeY until rangeY - halfRangeY) {
            //todo :不完美
            val transform = if (abs(pitch) == 90.0 && abs(yaw) == 90.0) {
                Location(world, 0.0, x.toDouble(), y.toDouble()).transform(transformMatrix)
            } else Location(world, x.toDouble(), y.toDouble(), 0.0).transform(transformMatrix)
            set.add(transform.block)
        }
    }
    set.remove(target)
    return set
}

fun Location.transform(transformMatrix: Matrix<Double>): Location {
    val pointMatrix = matrixOf(
        1, 4,
        x, y, z, 1.0
    )
    val matrix = transformMatrix dot pointMatrix
    return Location(world, matrix[0][0], matrix[1][0], matrix[2][0])
}

fun getTransformMatrix(location: Location, angleX: Double, angleY: Double, angleZ: Double): Matrix<Double> {
    val zeroX = location.x + 0.5
    val zeroY = location.y + 0.5
    val zeroZ = location.z + 0.5
    val moveMatrix = matrixOf(
        4, 4,
        1.0, 0.0, 0.0, zeroX,
        0.0, 1.0, 0.0, zeroY,
        0.0, 0.0, 1.0, zeroZ,
        0.0, 0.0, 0.0, 1.0
    )
    val transformMatrixX = matrixOf(
        4, 4,
        1.0, 0.0, 0.0, 0.0,
        0.0, cos(angleX), -sin(angleX), 0.0,
        0.0, sin(angleX), cos(angleX), 0.0,
        0.0, 0.0, 0.0, 1.0
    )
    val transformMatrixY = matrixOf(
        4, 4,
        cos(angleY), 0.0, sin(angleY), 0.0,
        0.0, 1.0, 0.0, 0.0,
        -sin(angleY), 0.0, cos(angleY), 0.0,
        0.0, 0.0, 0.0, 1.0
    )
    val transformMatrixZ = matrixOf(
        4, 4,
        cos(angleZ), -sin(angleZ), 0.0, 0.0,
        sin(angleZ), cos(angleZ), 0.0, 0.0,
        0.0, 0.0, 1, 0.0,
        0.0, 0.0, 0.0, 1.0
    )
    val rotateMatrix = transformMatrixZ dot transformMatrixX dot transformMatrixY

    return moveMatrix dot rotateMatrix
}
