/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/28 下午7:06
 *
 */

package top.iseason.mmoforge.event

import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.inventory.ItemStack


class MMOBlockBreakEvent(
    theBlock: Block,
    val player: Player,
    val mmoItem: LiveMMOItem,
    val handItem: ItemStack,
    val parent: BlockBreakEvent
) : BlockEvent(theBlock), Cancellable {

    inline fun <reified T : StatData> getMMOData(stat: ItemStat): T? {
        if (!stat.isEnabled) return null
        return mmoItem.getData(stat) as? T
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }


    companion object {

        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    var isDropItems
        get() = parent.isDropItems
        set(value) {
            parent.isDropItems = value
        }
    var expToDrop
        get() = parent.expToDrop
        set(value) {
            parent.expToDrop = value
        }

    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}