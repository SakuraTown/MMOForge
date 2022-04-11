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

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import top.iseason.mmoforge.uitls.getTransformMatrix
import top.iseason.mmoforge.uitls.transform

object EventListener : Listener {

    @EventHandler
    fun test(event: PlayerJoinEvent) {
//        ScopeTester(event.player, 5, 5, 1).runTaskTimer(MMOForge.instance, 0L, 5L)
    }

}

class ScopeTester(val player: Player, val x: Int, val y: Int, val z: Int) : BukkitRunnable() {
    override fun run() {
        if (!player.isOnline) {
            cancel()
            return
        }
        val world = player.location.world
        val eyePitch = player.eyeLocation.pitch
        val direction = player.eyeLocation.direction
        val target = player.eyeLocation.add(direction.multiply(5))
        val eyeYaw = player.eyeLocation.yaw
        val transformMatrix =
            getTransformMatrix(
                target.x,
                target.y,
                target.z,
                eyePitch / 180.0 * Math.PI,
                -eyeYaw / 180.0 * Math.PI,
                0.0
            )
        val halfRangeX = x / 2
        val halfRangeY = y / 2
        for (x in -halfRangeX until x - halfRangeX) {
            for (y in -halfRangeY until y - halfRangeY) {
                for (z in 0 until z) {
                    val transform = Location(world, x.toDouble(), y.toDouble(), z.toDouble()).transform(transformMatrix)
                    world.spawnParticle(Particle.NOTE, transform, 1)
                }

            }
        }
    }

}