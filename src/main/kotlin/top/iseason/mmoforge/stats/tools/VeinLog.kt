/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/15 下午8:53
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.common.submit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import top.iseason.mmoforge.uitls.checkMainHand
import top.iseason.mmoforge.uitls.getVeinBlocks

object VeinLog : MMOAttribute(
    "VEIN_LOG",
    Material.CHAINMAIL_CHESTPLATE,
    "Vein Log",
    "&7■ &f伐木连锁: &a# &f个",
    arrayOf("连锁采集原木"),
    arrayOf("tool")
) {
    private val veiningSet = mutableSetOf<Player>()
    private val veinSet = mutableSetOf<Material>().apply {
        this += Tag.LOGS.values
        this += Tag.LEAVES.values
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        if (veiningSet.contains(player)) return
        val type = event.block.type
        if (type !in veinSet) return
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