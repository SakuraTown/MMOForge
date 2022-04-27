/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/3 下午1:20
 *
 */

/*
 * Description:
 * 事件监听类
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午1:38
 *
 */

package top.iseason.mmoforge.listener

import com.entiv.core.utils.bukkit.applyMeta
import com.entiv.core.utils.toRoman
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.event.ItemBuildEvent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import top.iseason.mmoforge.stats.ForgeStat
import top.iseason.mmoforge.uitls.getRelativeCoordinate
import top.iseason.mmoforge.uitls.getTransformMatrix
import top.iseason.mmoforge.uitls.setName
import top.iseason.mmoforge.uitls.transform

object EventListener : Listener {

    @EventHandler
    fun onItemBuildEvent(event: ItemBuildEvent) {
        val itemStack = event.itemStack ?: return
        val item = NBTItem.get(itemStack) ?: return
        val string = item.getString(ForgeStat.nbtPath)
        val result = Regex("\"refine\":(.?),").find(string) ?: return
        val refine = result.groupValues[1].toInt()
        itemStack.applyMeta {
            setName("$displayName ${refine.toRoman()}")
        }
    }

    @EventHandler
    fun test(event: ItemBuildEvent) {
//        ScopeTester(event.player, 5, 5, 1).runTaskTimer(MMOForge.instance, 0L, 5L)
    }

}

class ScopeTester(val player: Player, val x: Int, val y: Int, val z: Int) : BukkitRunnable() {
    var tz = 0.0
    var scal = 1.0
    override fun run() {
        if (!player.isOnline) {
            cancel()
            return
        }
        val relativeCoordinate = player.getRelativeCoordinate()
        val eyeLocation = player.eyeLocation
//        player.world.spawnParticle(
//            Particle.REDSTONE,
//            eyeLocation.clone().add(relativeCoordinate[0].clone().multiply(2)),
//            1,
//            Particle.DustOptions(Color.RED, 1F)
//        )
//        player.world.spawnParticle(
//            Particle.REDSTONE,
//            eyeLocation.clone().add(relativeCoordinate[1].clone().multiply(2)),
//            1,
//            Particle.DustOptions(Color.GREEN, 1F)
//        )
//        player.world.spawnParticle(
//            Particle.REDSTONE,
//            eyeLocation.clone().add(relativeCoordinate[2].clone().multiply(2)),
//            1,
//            Particle.DustOptions(Color.BLUE, 1F)
//        )

        val halfRangeX = x / 2
        val halfRangeY = y / 2
//        for (x in -halfRangeX until x - halfRangeX) {
//            //高
//            for (y in -halfRangeY until y - halfRangeY) {
//                //深度,从被挖的方块往里
//                for (z in 0 until z) {
//                    val location =
//                        player.getLocationRelativelyByCoordinate(relativeCoordinate, x.toDouble(), y.toDouble(), 5.0)
//                    player.world.spawnParticle(Particle.NOTE, location.block.location, 1)
//                }
//            }
//        }
        val eyePitch = eyeLocation.pitch / 180.0 * Math.PI
        //Y轴旋转角(角度 转 弧度)
        val eyeYaw = eyeLocation.yaw / 180.0 * Math.PI
        val location = eyeLocation.add(eyeLocation.direction.multiply(5))
        //生成变换矩阵
        val transformMatrix =
            getTransformMatrix(
                location.x + 0.5,
                location.y + 0.5,
                location.z + 0.5,
                eyePitch,
                -eyeYaw,
                tz / 180.0 * Math.PI,
                scal,
                scal,
                scal
            )
        tz = ((tz + 10.0) % 360.0)
        scal = (scal + 0.5) % 2.0
        //宽
        for (x in -halfRangeX until x - halfRangeX) {
            //高
            for (y in -halfRangeY until y - halfRangeY) {
                //深度,从被挖的方块往里
                for (z in 0 until z) {
                    val loc = Location(
                        player.world,
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble()
                    ).transform(transformMatrix)
                    player.world.spawnParticle(Particle.REDSTONE, loc, 1, Particle.DustOptions(Color.RED, 1.0F))
                }
            }
        }

    }

}