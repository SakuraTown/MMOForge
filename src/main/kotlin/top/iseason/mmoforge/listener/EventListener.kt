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

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable
import top.iseason.mmoforge.uitls.getLocationRelativelyByCoordinate
import top.iseason.mmoforge.uitls.getRelativeCoordinate

object EventListener : Listener {

    @EventHandler
    fun test(event: PlayerJoinEvent) {
//        ScopeTester(event.player, 3, 3, 1).runTaskTimer(MMOForge.instance, 0L, 5L)
    }

}

class ScopeTester(val player: Player, val x: Int, val y: Int, val z: Int) : BukkitRunnable() {
    override fun run() {
        if (!player.isOnline) {
            cancel()
            return
        }
        val relativeCoordinate = player.getRelativeCoordinate()
        val eyeLocation = player.eyeLocation
        player.world.spawnParticle(
            Particle.REDSTONE,
            eyeLocation.clone().add(relativeCoordinate[0].clone().multiply(2)),
            1,
            Particle.DustOptions(Color.RED, 1F)
        )
        player.world.spawnParticle(
            Particle.REDSTONE,
            eyeLocation.clone().add(relativeCoordinate[1].clone().multiply(2)),
            1,
            Particle.DustOptions(Color.GREEN, 1F)
        )
        player.world.spawnParticle(
            Particle.REDSTONE,
            eyeLocation.clone().add(relativeCoordinate[2].clone().multiply(2)),
            1,
            Particle.DustOptions(Color.BLUE, 1F)
        )
        val halfRangeX = x / 2
        val halfRangeY = y / 2
        for (x in -halfRangeX until x - halfRangeX) {
            //高
            for (y in -halfRangeY until y - halfRangeY) {
                //深度,从被挖的方块往里
                for (z in 0 until z) {
                    val location =
                        player.getLocationRelativelyByCoordinate(relativeCoordinate, x.toDouble(), y.toDouble(), 5.0)
                    player.world.spawnParticle(Particle.NOTE, location.block.location, 1)
                }
            }
        }

    }

}