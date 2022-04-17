/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/16 下午9:11
 *
 */

package top.iseason.mmoforge.attributes;

import net.Indyuce.mmoitems.stat.data.BooleanData
import net.Indyuce.mmoitems.stat.type.BooleanStat
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.getMMOData

object AutoTorch : MMOAttribute(
    "AUTO_TORCH",
    Material.IRON_PICKAXE,
    "Auto Torch",
    "&7■ &f自动火把",
    arrayOf("自动插火把"),
    arrayOf("tool")
) {
    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return
        val player = event.player
        if (player.equipment.itemInMainHand.getMMOData<BooleanData>(stat)?.isEnabled != true) return
        val location = player.location
        val floor = location.clone().apply { y -= 1.0 }
        val floorType = floor.block.type
        if (!floorType.isOccluding) return
        if (location.block.lightLevel > 7) return
        val block = location.block
        if (!block.type.isAir) return
        if (player.gameMode != GameMode.CREATIVE) {
            val inventory = player.inventory
            val index = inventory.first(Material.TORCH)
            if (index < 0) return
            inventory.getItem(index)?.subtract() ?: return
        }
        block.type = Material.TORCH
    }

    override val stat = BooleanStat(mID, mMaterial, mName, mLore, mTypes)
}
