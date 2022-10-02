/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/9/30 下午7:06
 *
 */

package top.iseason.bukkit.mmoforge.command

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.SoulboundData
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.stats.tools.SakuraSoulBound
import top.iseason.bukkit.mmoforge.ui.BreakThroughUI
import top.iseason.bukkit.mmoforge.ui.ForgeUI
import top.iseason.bukkit.mmoforge.ui.ReFineUI
import top.iseason.bukkit.mmoforge.uitls.setName
import top.iseason.bukkittemplate.command.*
import top.iseason.bukkittemplate.ui.openUI
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.other.NumberUtils.toRoman

fun mainCommands() {
    command("mmoforge") {
        alias = arrayOf("mf", "mforge", "mmof")
        node("break") {
            isPlayerOnly = true
            async = true
            description = "打开突破界面"
            executor {
                (it as Player).openUI(BreakThroughUI(it))
            }
        }
        node("forge") {
            isPlayerOnly = true
            async = true
            description = "打开强化界面"
            executor {
                (it as Player).openUI(ForgeUI(it))
            }
        }
        node("refine") {
            isPlayerOnly = true
            async = true
            description = "打开精炼界面"
            executor {
                (it as Player).openUI(ReFineUI(it))
            }
        }
        node("bind") {
            default = PermissionDefault.OP
            description = "绑定某个玩家的物品"
            async = true
            param("[player]", suggestRuntime = ParamSuggestCache.playerParam)
            executor {
                val player = nextOrNull<Player>() ?: it as? Player ?: throw ParmaException("该命令仅限玩家使用")
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
    }
}