/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/9/30 下午7:06
 *
 */

package top.iseason.bukkit.mmoforge.command

import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.mmoforge.MMOForge
import top.iseason.bukkit.mmoforge.config.*
import top.iseason.bukkit.mmoforge.ui.BreakThroughUI
import top.iseason.bukkit.mmoforge.ui.ForgeUI
import top.iseason.bukkit.mmoforge.ui.ReFineUI
import top.iseason.bukkittemplate.command.command
import top.iseason.bukkittemplate.command.executor
import top.iseason.bukkittemplate.command.node
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.bukkit.SchedulerUtils.submit

fun mainCommands() {
    command("mmoforge") {
        alias = arrayOf("mf", "mforge", "mmof")
        node("break") {
            isPlayerOnly = true
            async = true
            description = "打开突破界面"
            executor { _, it ->
                val build = BreakThroughUI(it as Player).build()
                submit {
                    it.openInventory(build)
                }
            }
        }
        node("breakfor") {
            async = true
            default = PermissionDefault.OP
            description = "为某玩家打开突破界面"
            executor { arg, it ->
                val player = arg.next<Player>()
                val build = BreakThroughUI(player).build()
                submit {
                    player.openInventory(build)
                }
            }
        }
        node("forge") {
            isPlayerOnly = true
            async = true
            description = "打开强化界面"
            executor { _, it ->
                val build = ForgeUI(it as Player).build()
                submit {
                    it.openInventory(build)
                }
            }
        }
        node("forgefor") {
            async = true
            default = PermissionDefault.OP
            description = "为某玩家打开强化界面"
            executor { arg, it ->
                val player = arg.next<Player>()
                val build = ForgeUI(player).build()
                submit {
                    player.openInventory(build)
                }
            }
        }
        node("refine") {
            isPlayerOnly = true
            async = true
            description = "打开精炼界面"
            executor { _, it ->
                val build = ReFineUI(it as Player).build()
                submit {
                    it.openInventory(build)
                }
            }
        }
        node("refinefor") {
            async = true
            default = PermissionDefault.OP
            description = "为某玩家打开精炼界面"
            executor { arg, it ->
                val player = arg.next<Player>()
                val build = ReFineUI(player).build()
                submit {
                    player.openInventory(build)
                }
            }
        }
        node("reload") {
            async = true
            default = PermissionDefault.OP
            description = "重载配置"
            executor { _, it ->
                BreakUIConfig.load()
                RefineUIConfig.load()
                ForgeUIConfig.load()
                MainConfig.load()
                Lang.load()
                it.sendColorMessage("&a重载成功!")
            }
        }
        node("reloadTools") {
            async = true
            default = PermissionDefault.OP
            description = "重载工具属性配置配置"
            executor { _, it ->
                for (statLoreFormat in MMOForge.statLoreFormats) {
                    statLoreFormat.load()
                }
                it.sendColorMessage("&a重载成功!")
            }
        }

//        node("bind") {
//            default = PermissionDefault.OP
//            description = "绑定某个玩家的物品"
//            async = true
//            param("[player]", suggestRuntime = ParamSuggestCache.playerParam)
//            executor { params, it ->
//                val player = params.nextOrNull<Player>() ?: it as? Player ?: throw ParmaException("该命令仅限玩家使用")
//                for ((index, itemStack) in player.inventory.withIndex()) {
//                    if (itemStack == null) continue
//                    val nbt = NBTItem.get(itemStack) ?: continue
//                    if (!nbt.hasType()) continue
//                    val liveMMOItem = LiveMMOItem(nbt)
//                    if (!liveMMOItem.hasData(SakuraSoulBound.stat)) continue
//                    if (liveMMOItem.hasData(ItemStats.SOULBOUND)) continue
//                    liveMMOItem.setData(ItemStats.SOULBOUND, SoulboundData(player, 10))
//                    val forgeData = liveMMOItem.getData(MMOForgeStat) as? MMOForgeData
//                    val build = liveMMOItem.newBuilder().buildSilently()
//                    if (forgeData != null)
//                        build.applyMeta {
//                            setName("$displayName ${forgeData.refine.toRoman()}")
//                        }
//                    player.inventory.setItem(index, build)
//                }
//            }
//        }
    }
}