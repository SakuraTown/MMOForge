/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/10 下午12:54
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.common.submit
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.*


object ScopeMiner : MMOAttribute(
    "SCOPE_MINER",
    Material.IRON_PICKAXE,
    "Scope Miner",
    "&7■ &f范围挖掘: &a# x # &f",
    arrayOf("挖掘更大的区域"),
    arrayOf("tool")
) {
    private val scopeSet = mutableSetOf<Player>()

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        if (player in scopeSet) return
        val hdType = player.equipment.itemInMainHand.type
        //指定类型的工具只能挖指定类型的东西
        val mineAbleBlocks: Set<Material> = when {
            hdType.isPickaxe() -> Tag.MINEABLE_PICKAXE.values
            hdType.isAxe() -> Tag.MINEABLE_AXE.values
            hdType.isShovel() -> Tag.MINEABLE_SHOVEL.values
            else -> return
        }
        val level = player.checkMainHand(stat) ?: return
        scopeSet += player
        val count = level.toInt() + 2
        val rangeX = count / 2
        val rangeY = count - rangeX
        val scopeBlocks = player.getScopeBlocksByVector(event.block, rangeX, rangeY, 1)
        val iterator = scopeBlocks.iterator()
        submit(period = 1L) {
            repeat(10) {
                if (!iterator.hasNext()) {
                    scopeSet -= player
                    cancel()
                    return@submit
                }
                val block = iterator.next()
                if (block.type !in mineAbleBlocks) {
                    return@repeat
                }
                player.breakBlock(block)
            }
        }
    }

    override var loreAction: DoubleStat.(DoubleData) -> String = {
        val count = it.value.toInt() + 2
        val x = count / 2
        val y = count - x
        MMOItems.plugin.language.getStatFormat(path).replaceFirst("#", x.toString())
            .replaceFirst("#", y.toString())
    }
}