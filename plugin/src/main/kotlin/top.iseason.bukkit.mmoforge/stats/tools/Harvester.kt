/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/15 下午11:14
 *
 */

package top.iseason.bukkit.mmoforge.stats.tools

import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.scheduler.BukkitTask
import top.iseason.bukkit.mmoforge.event.MMOBlockBreakEvent
import top.iseason.bukkit.mmoforge.uitls.getFlatBlocksByMatrix
import top.iseason.bukkit.mmoforge.uitls.isHoe
import top.iseason.bukkittemplate.utils.other.submit

object Harvester : MMOAttribute(
    "HARVESTER",
    Material.IRON_PICKAXE,
    "Harvester",
    "&7■ &f播种机: &a# x # &f",
    arrayOf("范围播种"),
    arrayOf("tool")
) {
    private val scopeSet = mutableSetOf<Player>()

    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockBreakEvent(event: MMOBlockBreakEvent) {
        val player = event.player
        if (player in scopeSet) return
        val hdType = event.handItem.type
        //指定类型的工具只能挖指定类型的东西
        if (!hdType.isHoe()) return
        val level = event.getMMOData<DoubleData>(stat)?.value ?: return
        scopeSet += player
        val count = level.toInt() + 2
        val rangeX = count / 2
        val rangeY = count - rangeX
        val scopeBlocks = player.getFlatBlocksByMatrix(event.block, rangeX, rangeY)
        val iterator = scopeBlocks.iterator()
        val mineAbleBlocks = Tag.CROPS.values
        var task: BukkitTask? = null
        task = submit(period = 1L) {
            repeat(10) {
                if (!iterator.hasNext()) {
                    scopeSet -= player
                    task?.cancel()
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

    override fun formatLore(stat: DoubleStat, data: DoubleData): String {
        val count = data.value.toInt() + 2
        val x = count / 2
        val y = count - x
        return MMOItems.plugin.language.getStatFormat(stat.path).replaceFirst("#", x.toString())
            .replaceFirst("#", y.toString())
    }
}