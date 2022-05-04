/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/5/1 下午4:04
 *
 */

package top.iseason.mmoforge.command

import com.entiv.core.command.CompositeCommand
import com.entiv.core.command.SimpleSubcommand
import com.entiv.core.common.submit
import com.entiv.core.plugin.SimplePlugin
import com.entiv.core.utils.bukkit.applyMeta
import com.entiv.core.utils.toRoman
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.SoulboundData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.stats.MMOForgeStat
import top.iseason.mmoforge.stats.tools.SakuraSoulBound
import top.iseason.mmoforge.uitls.setName

class SoulBindCommand(parent: CompositeCommand) : SimpleSubcommand(
    plugin = SimplePlugin.instance,
    name = "bind",
    adminOnly = false,
    parent = parent,
    usage = "${parent.name} bind <player>",
    description = "绑定某个玩家的物品",
) {
    override fun onCommand() {
        if (args.isEmpty()) {
            val player = sender as? Player ?: return
            submit(async = true) {
                bindInventory(player)
            }
            return
        }
        if (args.size == 1) {
            val player = Bukkit.getPlayer(args[0]) ?: return
            submit(async = true) {
                bindInventory(player)
            }
        }
    }

    private fun bindInventory(player: Player) {
        for ((index, itemStack) in player.inventory.withIndex()) {
            if (itemStack == null) continue
            val nbt = NBTItem.get(itemStack) ?: continue
            if (!nbt.hasType()) continue
            val liveMMOItem = LiveMMOItem(nbt)
            if (!liveMMOItem.hasData(SakuraSoulBound.stat)) continue
            if (liveMMOItem.hasData(ItemStats.SOULBOUND)) continue
            liveMMOItem.setData(ItemStats.SOULBOUND, SoulboundData(player, 10))
            val forgeData = liveMMOItem.getData(MMOForgeStat) as? MMOForgeData
            val build = liveMMOItem.newBuilder().buildSilently()
            if (forgeData != null)
                build.applyMeta {
                    setName("$displayName ${forgeData.refine.toRoman()}")
                }
            player.inventory.setItem(index, build)
        }
    }
}
