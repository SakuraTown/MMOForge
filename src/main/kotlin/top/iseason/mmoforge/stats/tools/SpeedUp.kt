/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/17 下午1:25
 *
 */

package top.iseason.mmoforge.stats.tools

import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import top.iseason.mmoforge.event.MMOBlockBreakEvent

object SpeedUp : MMOAttribute(
    "SPEED_UP",
    Material.CHAINMAIL_CHESTPLATE,
    "Speed Up",
    "&7■ &f加速: &a#",
    arrayOf("挖矿时获得速度加成"),
    arrayOf("tool")
) {
    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockBreakEvent(event: MMOBlockBreakEvent) {
        val player = event.player
        val level = event.getMMOData<DoubleData>(stat)?.value?.toInt()?.minus(1) ?: return
        if (level < 0) return
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, level))
    }
}