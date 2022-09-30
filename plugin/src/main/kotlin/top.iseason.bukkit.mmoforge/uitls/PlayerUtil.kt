/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/24 下午11:35
 *
 */

package top.iseason.bukkit.mmoforge.uitls

import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import top.iseason.bukkittemplate.utils.bukkit.LocationUtils.getNormalX
import top.iseason.bukkittemplate.utils.bukkit.LocationUtils.getNormalZ


fun Player.checkMainHandData(stat: ItemStat<*, *>): Double? =
    equipment?.itemInMainHand?.getMMOData<DoubleData>(stat)?.value

/**
 * 在方块处掉落该方块相应的掉落物(使用指定工具),不包括破坏方块
 */
fun Player.dropBlock(block: Block, tool: ItemStack?) {
    val drops = block.getDrops(tool)
    val itemList = mutableListOf<Item>()
    for (drop in drops) {
        itemList.add(world.dropItemNaturally(block.location, drop))
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
fun Player.getScopeBlocksByMatrix(target: Block, rangeX: Int, rangeY: Int, rangeZ: Int): Set<Block> {
    val set = mutableSetOf<Block>()
    val location = target.location
    location.direction
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
            -eyeYaw
        )
    val halfRangeX = rangeX / 2
    val halfRangeY = rangeY / 2
    //宽
    for (x in -halfRangeX until rangeX - halfRangeX) {
        //高
        for (y in -halfRangeY until rangeY - halfRangeY) {
            //深度,从被挖的方块往里
            for (z in 0 until rangeZ) {
                val block = Location(world, x.toDouble(), y.toDouble(), z.toDouble()).transform(transformMatrix).block
                set.add(block)
            }
        }
    }
    return set
}

/**
 * 由相对坐标系算法获取相对范围内的方块
 * @param target 起始方块
 * @param rangeX 宽度
 * @param rangeY 高度
 * @param rangeZ 深度
 * @return 所有方块的集合(除了 target)
 */
fun Player.getScopeBlocksByVector(target: Block, rangeX: Int, rangeY: Int, rangeZ: Int): Set<Block> {
    val set = mutableSetOf<Block>()
    //玩家与准星上的方块之间的距离
    val baseX = target.location.apply {
        x += 0.5
        y += 0.5
        z += 0.5
    }.distance(eyeLocation)
    val halfRangeX = rangeX / 2
    val halfRangeY = rangeY / 2
    //获取相对坐标系
    val relativeCoordinate = getRelativeCoordinate()
    val eyeLocation = eyeLocation
    //宽
    for (x in -halfRangeX until rangeX - halfRangeX) {
        //高
        for (y in -halfRangeY until rangeY - halfRangeY) {
            //深度,从被挖的方块往里
            for (z in 0 until rangeZ) {
                //相对坐标转世界坐标并获取对应的方块
                val block = eyeLocation.getRelativeByCoordinate(
                    relativeCoordinate,
                    x.toDouble(),
                    y.toDouble(),
                    baseX + z.toDouble()
                ).block
                set.add(block)
            }
        }
    }
    return set
}

/**
 *获取方块平面 x*y 的所有方块
 * @param target 起始方块
 * @param rangeX 长度
 * @param rangeY 宽度
 * @return 所有方块的集合(除了 target)
 */
fun Player.getFlatBlocksByMatrix(target: Block, rangeX: Int, rangeY: Int): Set<Block> {
    val halfRangeX = rangeX / 2
    val halfRangeY = rangeY / 2
    val location = target.location
    val eyeYaw = eyeLocation.yaw / 180.0 * Math.PI
    val world = world
    //生成变换矩阵
    val transformMatrix = getTransformMatrix(location.x + 0.5, location.y + 0.5, location.z + 0.5, 0.0, -eyeYaw)
    val set = mutableSetOf<Block>()
    for (x in -halfRangeX until rangeX - halfRangeX) {
        for (y in -halfRangeY until rangeY - halfRangeY) {
            set.add(Location(world, x.toDouble(), 0.0, y.toDouble()).transform(transformMatrix).block)
        }
    }
    return set
}


// 由相对坐标及相对坐标系获取世界坐标
fun Player.getLocationRelativelyByCoordinate(coordinate: Array<Vector>, x: Double, y: Double, z: Double): Location {
    return eyeLocation.getRelativeByCoordinate(coordinate, x, y, z)
}

// 由相对坐标系获取世界坐标
fun Player.getRelativeByCoordinate(x: Double, y: Double, z: Double): Location {
    return getLocationRelativelyByCoordinate(getRelativeCoordinate(), x, y, z)
}

/**
 * 获取玩家相对坐标系的3个坐标轴在世界坐标系下的单位向量
 * @return 一个向量数组, index 0 为 X轴，1 为 Y轴，2 为Z轴
 */
fun Player.getRelativeCoordinate(): Array<Vector> {
    val eyeLocation = eyeLocation
    val normalX = eyeLocation.getNormalX()  // X 轴 其实就是getDirection()方法，我改了个名字
    val normalZ = eyeLocation.getNormalZ()  //Z 轴
    val normalY = normalX.clone().crossProduct(normalZ).multiply(-1) //叉乘得出Y轴，测试发现朝下，于是乘以-1使其朝上
    return arrayOf(normalX, normalY, normalZ)
}


