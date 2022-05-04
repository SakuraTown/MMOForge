/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/3 下午1:20
 *
 */

/*
 * Description:
 * 事件监听类
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午1:38
 *
 */

package top.iseason.mmoforge.listener

import com.entiv.core.utils.bukkit.applyMeta
import com.entiv.core.utils.toRoman
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.event.ItemBuildEvent
import net.Indyuce.mmoitems.api.event.item.ApplyGemStoneEvent
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import top.iseason.mmoforge.event.MMOBlockBreakEvent
import top.iseason.mmoforge.event.MMOBlockDropItemEvent
import top.iseason.mmoforge.stats.MMOForgeStat
import top.iseason.mmoforge.uitls.setName

object MMOListener : Listener {
    //锁库
    private val eventLock = HashMap<Class<Event>, HashSet<Any>>()

    @EventHandler(ignoreCancelled = true)
    fun callMMOBreakBlockEvent(event: BlockBreakEvent) {
        val player = event.player
        if (isLockEvent(event, event.block)) return
        val itemInMainHand = player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val nbt = NBTItem.get(itemInMainHand)
        if (!nbt.hasType()) return
        val liveMMOItem = LiveMMOItem(nbt)
        val mmoEvent = MMOBlockBreakEvent(
            event.block,
            event.player,
            liveMMOItem,
            itemInMainHand,
            event
        )

        Bukkit.getPluginManager().callEvent(mmoEvent)

        with(event) {
            this.isCancelled = mmoEvent.isCancelled
            this.isDropItems = mmoEvent.isDropItems
            this.expToDrop = mmoEvent.expToDrop
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun callMMOBlockDropItemEvent(event: BlockDropItemEvent) {
        val player = event.player
        //目前还不需要锁
//        if (isLockEvent(event, event.block)) return
        val itemInMainHand = player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val nbt = NBTItem.get(itemInMainHand)
        if (!nbt.hasType()) return
        val liveMMOItem = LiveMMOItem(nbt)
        val mmoEvent = MMOBlockDropItemEvent(
            event.block,
            event.blockState,
            event.player,
            event.items,
            liveMMOItem,
            itemInMainHand,
            event
        )
        Bukkit.getPluginManager().callEvent(mmoEvent)
        event.isCancelled = mmoEvent.isCancelled
    }

    /**
     * 检查MMO物品生成，并修改添加精炼
     */
    @EventHandler
    fun onItemBuildEvent(event: ItemBuildEvent) {
        val itemStack = event.itemStack ?: return
        val item = NBTItem.get(itemStack) ?: return
        val string = item.getString(MMOForgeStat.nbtPath)
        val result = Regex("\"refine\":(.?),").find(string) ?: return
        val refine = result.groupValues[1].toInt()
        itemStack.applyMeta {
            setName("$displayName ${refine.toRoman()}")
        }
    }

    @EventHandler
    fun onApplyGemStoneEvent(event: ApplyGemStoneEvent) {
        val targetItem = event.targetItem
        println(targetItem.hasData(MMOForgeStat))
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    fun onEntityPickupItemEvent(event: EntityPickupItemEvent) {
//        val player = event.entity as? Player ?: return
//        val item = event.item.itemStack
//        val nbt = NBTItem.get(item) ?: return
//        if (!nbt.hasType()) return
//        val liveMMOItem = LiveMMOItem(nbt)
//        if (!liveMMOItem.hasData(SakuraSoulBound.stat)) return
//        if (liveMMOItem.hasData(ItemStats.SOULBOUND)) return
//        liveMMOItem.setData(ItemStats.SOULBOUND, SoulboundData(player, 10))
//        event.item.itemStack = liveMMOItem.newBuilder().build() ?: return
//    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
//    fun onInventoryClickEvent(event: InventoryClickEvent) {
//        val item = event.currentItem ?: return
//        val nbt = NBTItem.get(item) ?: return
//        if (!nbt.hasType()) return
//        val liveMMOItem = LiveMMOItem(nbt)
//        if (!liveMMOItem.hasData(SakuraSoulBound.stat)) return
//        if (liveMMOItem.hasData(ItemStats.SOULBOUND)) return
//        liveMMOItem.setData(ItemStats.SOULBOUND, SoulboundData(event.whoClicked as Player, 10))
////        event.isCancelled = true
//        event.view.setItem(event.rawSlot, liveMMOItem.newBuilder().build() ?: return)
//    }
//
//    @EventHandler
//    fun onPlayerDropItemEvent(event: PlayerDropItemEvent) {
//        val item = event.itemDrop.itemStack
//        val nbt = NBTItem.get(item) ?: return
//        if (!nbt.hasType()) return
//        val liveMMOItem = LiveMMOItem(nbt)
//        if (!liveMMOItem.hasData(SakuraSoulBound.stat)) return
//        if (liveMMOItem.hasData(ItemStats.SOULBOUND)) return
//        liveMMOItem.setData(ItemStats.SOULBOUND, SoulboundData(event.player, 10))
//        event.itemDrop.itemStack = liveMMOItem.newBuilder().build() ?: return
//    }

    /**
     * 将某个事件的某个对象上锁，不再触发，请注意即使解锁
     */
    fun lockEvent(event: Event, obj: Any) {
        val javaClass = event.javaClass
        if (!eventLock.containsKey(javaClass)) {
            eventLock[javaClass] = HashSet()
        }
        val set = eventLock[javaClass]!!
        set.add(obj)
    }

    /**
     * 检查某个事件的某个对象是否上锁
     */
    fun isLockEvent(event: Event, obj: Any): Boolean {
        val javaClass = event.javaClass
        if (!eventLock.containsKey(javaClass)) return false
        val set = eventLock[javaClass]!!
        return set.contains(obj)
    }

    /**
     * 将某个事件已上锁的对象解锁
     */
    fun unLockEvent(event: Event, obj: Any) {
        val javaClass = event.javaClass
        if (!eventLock.containsKey(javaClass)) return
        val set = eventLock[javaClass]!!
        set.remove(obj)
    }


}
