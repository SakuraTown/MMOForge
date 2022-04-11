/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:27
 *
 */

package top.iseason.mmoforge.attributes

import com.entiv.core.common.submit
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.getMMOData
import top.iseason.mmoforge.uitls.getVeinBlocks
import top.iseason.mmoforge.uitls.isOre

object VeinMiner : MMOEnchant(
    "VEIN_MINER_ORE",
    Material.CHAINMAIL_CHESTPLATE,
    "Vein Miner ore",
    "&7■ &f矿物连锁: &a# &f个",
    arrayOf("连锁采集矿物"),
    arrayOf("tool")
) {
    private val veiningSet = mutableSetOf<Player>()
    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        if (veiningSet.contains(player)) return
        val itemInMainHand = player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        if (!event.block.type.isOre()) return
        val level = itemInMainHand.getMMOData<DoubleData>(stat)?.value ?: return
        val veinBlocksIterator = getVeinBlocks(event.block, level.toInt()).iterator()
        veiningSet.add(player)
        //降低负担,每tick处理10个
        submit(period = 1L) {
            repeat(10) {
                if (!veinBlocksIterator.hasNext()) {
                    veiningSet.remove(player)
                    cancel()
                    return@submit
                }
                player.breakBlock(veinBlocksIterator.next())
            }
        }
    }
}