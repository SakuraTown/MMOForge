/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2023/6/10 下午8:16
 *
 */

package top.iseason.bukkittemplate.hook

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack

object ItemsAdderHook : BaseHook("ItemsAdder") {
    init {
        checkHooked()
    }

    fun isItemsAdderItem(item: ItemStack) = CustomStack.byItemStack(item) != null

    fun getItemsAdderId(item: ItemStack): String? = CustomStack.byItemStack(item)?.namespacedID
    fun getItemsAdderNamespace(item: ItemStack) = CustomStack.byItemStack(item)?.namespace
    fun getByNameId(id: String) = CustomStack.getInstance(id)?.itemStack
}
