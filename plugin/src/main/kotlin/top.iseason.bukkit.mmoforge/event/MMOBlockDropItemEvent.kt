/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/28 下午7:33
 *
 */

package top.iseason.bukkit.mmoforge.event

import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.inventory.ItemStack


class MMOBlockDropItemEvent(
    block: Block,
    val blockState: BlockState,
    val player: Player,
    val items: List<Item>,
    val mmoItem: LiveMMOItem,
    val handItem: ItemStack,
    val parent: BlockDropItemEvent
) : BlockEvent(block), Cancellable {

    inline fun <reified T : StatData> getMMOData(stat: ItemStat<*, *>): T? {
        if (!stat.isEnabled) return null
        return mmoItem.getData(stat) as? T
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    companion object {

        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}
