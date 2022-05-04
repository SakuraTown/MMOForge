/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/7 下午4:57
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.utils.RandomUtils
import com.entiv.core.utils.bukkit.isOre
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import top.iseason.mmoforge.event.MMOBlockDropItemEvent


object FortuneOre : MMOAttribute(
    "ORE_FORTUNE",
    Material.DIAMOND_PICKAXE,
    "Ore Fortune",
    "&7■ &f矿物时运 &a#",
    arrayOf("挖掘矿物时有概率凋落物增加"),
    arrayOf("tool")
) {
    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockDropItemEvent(event: MMOBlockDropItemEvent) {
        val type = event.blockState.type
        if (!type.isOre()) return
        val level = event.getMMOData<DoubleData>(stat)?.value ?: return
        for (item in event.items) {
            val itemStack = item.itemStack
            if (type == itemStack.type) continue
            itemStack.amount *= RandomUtils.calculateFortune(level.toInt())
        }
        event.player.facing
    }
}
