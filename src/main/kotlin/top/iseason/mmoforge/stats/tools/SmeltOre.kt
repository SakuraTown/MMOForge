/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/15 下午1:55
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.config.Comment
import com.entiv.core.config.ConfigState
import com.entiv.core.config.Key
import com.entiv.core.utils.RandomUtils
import com.entiv.core.utils.bukkit.isPickaxe
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import top.iseason.mmoforge.event.MMOBlockDropItemEvent

object SmeltOre : MMOAttribute(
    "SMELT_ORE",
    Material.DIAMOND_PICKAXE,
    "Smelt Ore",
    "&7■ &f熔炼: &a# &f%",
    arrayOf("挖掘矿物时有概率熔炼矿物"),
    arrayOf("tool")
) {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onMMOBlockDropItemEvent(event: MMOBlockDropItemEvent) {
        //镐子专属
        if (!event.handItem.type.isPickaxe()) return
        val percentage = event.getMMOData<DoubleData>(stat)?.value ?: return
        if (RandomUtils.checkPercentage(percentage)) return
        val type = event.blockState.type
        val ingot = smeltDrops[type] ?: return
        for (item in event.items) {
            item.itemStack.type = ingot
        }
    }

    private var smeltDrops = mutableMapOf<Material, Material>()

    @Comment("方块对应熔炼的物品")
    @Key("smeltDrops")
    var smeltDropsKey: MemorySection = YamlConfiguration().apply {
        set("IRON_ORE", "IRON_INGOT")
        set("GOLD_ORE", "GOLD_INGOT")
        set("COPPER_ORE", "COPPER_INGOT")
        set("DEEPSLATE_IRON_ORE", "IRON_INGOT")
        set("DEEPSLATE_GOLD_ORE", "GOLD_INGOT")
        set("DEEPSLATE_COPPER_ORE", "COPPER_INGOT")
        set("ANCIENT_DEBRIS", "NETHERITE_SCRAP")
        set("NETHERRACK", "NETHER_BRICK")

    }

    //自动处理映射
    override val onLoad: (ConfigState) -> Unit = {
        smeltDrops.clear()
        for (key in smeltDropsKey.getKeys(false)) {
            val ore = try {
                Material.valueOf(key)
            } catch (e: Exception) {
                continue
            }
            val ingot = try {
                val string = smeltDropsKey.getString(key) ?: continue
                Material.valueOf(string)
            } catch (e: Exception) {
                continue
            }
            smeltDrops[ore] = ingot
        }
    }

}