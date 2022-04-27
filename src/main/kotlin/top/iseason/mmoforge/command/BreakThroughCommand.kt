/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/27 上午1:51
 *
 */

package top.iseason.mmoforge.command

import com.entiv.core.command.CompositeCommand
import com.entiv.core.command.SimpleSubcommand
import com.entiv.core.plugin.SimplePlugin
import org.bukkit.entity.Player
import top.iseason.mmoforge.ui.BreakThroughUI

class BreakThroughCommand(parent: CompositeCommand) : SimpleSubcommand(
    plugin = SimplePlugin.instance,
    name = "break",
    adminOnly = false,
    parent = parent,
    usage = "${parent.name} break",
    description = "打开突破界面",
) {
    override fun onCommand() {
        (sender as? Player)?.openInventory(BreakThroughUI().inventory)
    }
}