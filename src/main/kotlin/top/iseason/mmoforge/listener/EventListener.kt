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

import org.bukkit.event.Listener

object EventListener : Listener {

//    @EventHandler
//    fun test(event: PlayerJoinEvent) {
//        Bukkit.getScheduler().runTaskTimer(MMOForge.instance, Runnable {
//            val player = event.player
//            val world = player.location.world
//            val eyePitch = player.eyeLocation.pitch
//            val direction = player.eyeLocation.direction
//            val target = player.eyeLocation.add(direction.multiply(5))
//            val eyeYaw = player.eyeLocation.yaw
//            val rangeX = 2
//            val rangeY = 3
//            val transformMatrix =
//                getTransformMatrix(target, eyePitch / 180.0 * Math.PI, -eyeYaw / 180.0 * Math.PI, 0.0)
//            val halfRangeX = rangeX / 2
//            val halfRangeY = rangeY / 2
//            for (x in -halfRangeX until rangeX - halfRangeX) {
//                for (y in -halfRangeY until rangeY - halfRangeY) {
//                    val transform = Location(world, x.toDouble(), y.toDouble(), 0.0).transform(transformMatrix)
//                    world.spawnParticle(Particle.NOTE, transform, 1)
//                }
//            }
//        }, 0L, 1L)
//    }

}
