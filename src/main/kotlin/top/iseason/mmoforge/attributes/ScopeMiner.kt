/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/10 下午12:54
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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.getMMOData
import top.iseason.mmoforge.uitls.getScopeBlocksByVector


object ScopeMiner : MMOEnchant(
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
        if (scopeSet.contains(player)) return
        val itemInMainHand = player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val level = itemInMainHand.getMMOData<DoubleData>(stat)?.value ?: return
        scopeSet.add(player)
        val count = level.toInt() + 2
        val rangeX = count / 2
        val rangeY = count - rangeX
        val scopeBlocks = player.getScopeBlocksByVector(event.block, rangeX, rangeY, 1)
        val iterator = scopeBlocks.iterator()
        submit(period = 1L) {
            repeat(10) {
                if (!iterator.hasNext()) {
                    scopeSet.remove(player)
                    cancel()
                    return@submit
                }
                player.breakBlock(iterator.next())
            }
        }
    }
//
//    @EventHandler
//    fun onBlockBreakEvent(event: BlockBreakEvent) {
//        val block = event.block //方块
//        val player = event.player //玩家
//        var facing = player.facing // BlockFace ->NORTH,EAST,SOUTH,WEST
//        val pitch = player.eyeLocation.pitch //0表示水平朝向.90表示向下,-90表示向上
//        facing = if (pitch < -45.0) BlockFace.DOWN else if (pitch > 45.0) BlockFace.UP else facing
//        //需要移动的坐标
//        val blockX = 0
//        val blockY = 0
//        val blockZ = 0
//        for (y in 1..3) {
//            for (z in 1..2) {
//                player.world.getBlockAt(0 + blockX, y + blockY, z + blockZ)
//            }
//        }
//    }


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