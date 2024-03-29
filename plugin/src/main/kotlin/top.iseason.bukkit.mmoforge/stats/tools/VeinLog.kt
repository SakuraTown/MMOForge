/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/15 下午8:53
 *
 */

package top.iseason.bukkit.mmoforge.stats.tools

import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.scheduler.BukkitTask
import top.iseason.bukkit.mmoforge.event.MMOBlockBreakEvent
import top.iseason.bukkit.mmoforge.listener.MMOListener
import top.iseason.bukkit.mmoforge.uitls.getVeinBlocks
import top.iseason.bukkittemplate.utils.bukkit.SchedulerUtils.submit

object VeinLog : MMOAttribute(
    "VEIN_LOG",
    Material.CHAINMAIL_CHESTPLATE,
    "Vein Log",
    "&7■ &f伐木连锁: &a{value} &f个",
    arrayOf("连锁采集原木"),
    arrayOf("tool")
) {
    private val veinSet = mutableSetOf<Material>().apply {
        this += Tag.LOGS.values
        this += Tag.LEAVES.values
    }

    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockBreakEvent(event: MMOBlockBreakEvent) {
        val player = event.player
        val type = event.block.type
        if (type !in veinSet) return
        val level = event.getMMOData<DoubleData>(stat)?.value ?: return
        val veinBlocksIterator = getVeinBlocks(event.block, level.toInt()).iterator()
        //降低负担,每tick处理10个
        var task: BukkitTask? = null
        task = submit(period = 1L) {
            repeat(10) {
                if (!veinBlocksIterator.hasNext()) {
                    task?.cancel()
                    return@submit
                }
                val block = veinBlocksIterator.next()
                MMOListener.lockEvent(event.parent, block)
                player.breakBlock(block)
                MMOListener.unLockEvent(event.parent, block)
            }
        }
    }
}