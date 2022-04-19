/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/15 下午11:14
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.common.submit
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.checkMainHand
import top.iseason.mmoforge.uitls.getFlatBlocksByMatrix
import top.iseason.mmoforge.uitls.isHoe

object Harvester : MMOAttribute(
    "HARVESTER",
    Material.IRON_PICKAXE,
    "Harvester",
    "&7■ &f播种机: &a# x # &f",
    arrayOf("范围播种"),
    arrayOf("tool")
) {
    private val scopeSet = mutableSetOf<Player>()

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        if (player in scopeSet) return
        val hdType = player.equipment.itemInMainHand.type
        //指定类型的工具只能挖指定类型的东西
        if (!hdType.isHoe()) return
        val level = player.checkMainHand(stat) ?: return
        scopeSet += player
        val count = level.toInt() + 2
        val rangeX = count / 2
        val rangeY = count - rangeX
        val scopeBlocks = player.getFlatBlocksByMatrix(event.block, rangeX, rangeY)
        val iterator = scopeBlocks.iterator()
        val mineAbleBlocks = Tag.CROPS.values
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
                val blockData = block.blockData
                val seed: Material?
                if (blockData is Ageable && blockData.age == blockData.maximumAge) {
                    seed = block.type
                } else return@repeat
                player.breakBlock(block)
                block.type = seed
            }
        }
        val block = event.block
        val bd = block.blockData
        if (bd is Ageable && bd.age == bd.maximumAge) {
            val type = block.type
            submit(delay = 1L) {
                block.type = type
            }
        }
    }

    override val loreAction: DoubleStat.(DoubleData) -> String = {
        val count = it.value.toInt() + 2
        val x = count / 2
        val y = count - x
        MMOItems.plugin.language.getStatFormat(path).replaceFirst("#", x.toString())
            .replaceFirst("#", y.toString())
    }
}