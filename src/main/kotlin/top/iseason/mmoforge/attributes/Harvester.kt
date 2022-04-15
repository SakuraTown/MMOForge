/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/15 下午11:14
 *
 */

package top.iseason.mmoforge.attributes

import com.entiv.core.common.submit
import io.lumine.mythic.lib.MythicLib
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.UpgradeTemplate
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.data.type.StatData
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
                val drill: Material?
                if (blockData is Ageable && blockData.age == blockData.maximumAge) {
                    drill = block.type
                } else return@repeat
                player.breakBlock(block)
                block.type = drill
            }
        }
        val block = event.block
        val bd = block.blockData
        if (bd is Ageable && bd.age == bd.maximumAge) {
            val type = block.type
            submit {
                block.type = type
            }
        }
    }

    override val stat: EnchantStat = object : EnchantStat() {
        override fun whenApplied(item: ItemStackBuilder, data: StatData) {
            val value = (data as DoubleData).value
            if (value < 0 && !handleNegativeStats()) {
                return
            }
            var upgradeShift = 0.0
            if (UpgradeTemplate.isDisplayingUpgrades() && item.mmoItem.upgradeLevel != 0) {
                val hist = item.mmoItem.getStatHistory(this)
                if (hist != null) {
                    val uData = hist.recalculateUnupgraded() as DoubleData
                    upgradeShift = value - uData.value
                }
            }
            if (value != 0.0 || upgradeShift != 0.0) {
                val count = value.toInt() + 2
                val x = count / 2
                val y = count - x
                var loreInsert: String? =
                    MMOItems.plugin.language.getStatFormat(path).replaceFirst("#", x.toString())
                        .replaceFirst("#", y.toString())

                if (upgradeShift != 0.0) loreInsert += MythicLib.plugin.parseColors(
                    UpgradeTemplate.getUpgradeChangeSuffix(
                        if (upgradeShift * multiplyWhenDisplaying() >= 0.0) "+" else "" + MythicLib.plugin.mmoConfig.decimals.format(
                            upgradeShift * multiplyWhenDisplaying()
                        ),
                        !isGood(upgradeShift * multiplyWhenDisplaying())
                    )
                )
                item.lore.insert(path, loreInsert)
            }
            if (data.value != 0.0) {
                item.addItemTag(getAppliedNBT(data))
            }
        }
    }
}