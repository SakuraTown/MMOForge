/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午7:16
 *
 */

package top.iseason.mmoforge.command

import com.entiv.core.command.CompositeCommand
import com.entiv.core.command.SimpleSubcommand
import com.entiv.core.plugin.SimplePlugin
import com.entiv.core.ui.openUI
import org.bukkit.entity.Player
import top.iseason.mmoforge.ui.ReFineUI

class ForgeCommand(parent: CompositeCommand) : SimpleSubcommand(
    plugin = SimplePlugin.instance,
    name = "forge",
    adminOnly = false,
    parent = parent,
    usage = "${parent.name} forge",
    description = "打开强化界面",
) {
    override fun onCommand() {
        (sender as? Player)?.openUI<ReFineUI>()
    }
}
