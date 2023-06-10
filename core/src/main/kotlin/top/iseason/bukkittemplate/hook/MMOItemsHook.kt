/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2023/6/10 下午8:16
 *
 */

package top.iseason.bukkittemplate.hook

import net.Indyuce.mmoitems.MMOItems
import org.bukkit.inventory.ItemStack

object MMOItemsHook : BaseHook("MMOItems") {
    init {
        checkHooked()
    }

    fun getByNameID(id: String): ItemStack? {
        if (!hasHooked) return null
        val split = id.split(':', limit = 2)
        return runCatching { MMOItems.plugin.getItem(split[0], split[1]) }.getOrNull()

    }

    fun isMMOItemsItem(item: ItemStack): Boolean {
        if (!hasHooked) return false
        return MMOItems.getType(item) != null
    }

    /**
     * type:id
     */
    fun getMMOItemsId(item: ItemStack): String? {
        if (!hasHooked) return null
        val type = MMOItems.getType(item)?.id ?: return null
        val id = MMOItems.getID(item) ?: return null
        return "$type:$id"
    }

    /**
     * type
     */
    fun getMMOItemsType(item: ItemStack): String? {
        if (!hasHooked) return null
        return MMOItems.getType(item)?.id
    }

}
