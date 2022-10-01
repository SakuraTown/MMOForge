/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/10/1 下午8:02
 *
 */

package top.iseason.bukkit.mmoforge.hook

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.iseason.bukkittemplate.hook.BaseHook
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.toColor

object PAPIHook : BaseHook("PlaceholderAPI") {

    fun setPlaceHolder(str: String, player: Player? = null): String {
        if (!hasHooked) return str
        return PlaceholderAPI.setPlaceholders(player, str)
    }

    fun setPlaceHolderAndColor(str: String, player: Player? = null): String = setPlaceHolder(str, player).toColor()

    fun setPlaceHolderAndColor(item: ItemStack, player: Player? = null): ItemStack = item.applyMeta {
        if (hasDisplayName())
            setDisplayName(setPlaceHolderAndColor(displayName, player))
        if (hasLore()) {
            lore = lore!!.map { setPlaceHolderAndColor(it, player) }
        }
    }
}