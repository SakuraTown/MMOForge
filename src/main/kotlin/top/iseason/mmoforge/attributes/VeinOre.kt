/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:27
 *
 */

package top.iseason.mmoforge.attributes

import com.entiv.core.common.submit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.checkMainHand
import top.iseason.mmoforge.uitls.getVeinBlocks
import top.iseason.mmoforge.uitls.isOre

object VeinOre : MMOAttribute(
    "VEIN_ORE",
    Material.CHAINMAIL_CHESTPLATE,
    "Vein ore",
    "&7■ &f矿物连锁: &a# &f个",
    arrayOf("连锁采集矿物"),
    arrayOf("tool")
) {
    private val veiningSet = mutableSetOf<Player>()

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        if (veiningSet.contains(player)) return
        if (!event.block.type.isOre()) return
        val level = player.checkMainHand(stat) ?: return
        val veinBlocksIterator = getVeinBlocks(event.block, level.toInt()).iterator()
        veiningSet.add(player)
        //降低负担,每tick处理10个
        submit(period = 1L) {
            repeat(10) {
                if (!veinBlocksIterator.hasNext()) {
                    if (player in veiningSet) {
                        veiningSet.remove(player)
                    }
                    cancel()
                    return@submit
                }
                player.breakBlock(veinBlocksIterator.next())
            }
        }
    }
}