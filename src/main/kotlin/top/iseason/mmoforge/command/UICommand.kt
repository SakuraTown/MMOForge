/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/3 下午1:15
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/2/5 下午11:09
 *
 */

package top.iseason.mmoforge.command

import com.entiv.core.command.CompositeCommand
import com.entiv.core.command.SimpleSubcommand
import com.entiv.core.plugin.SimplePlugin
import com.entiv.core.ui.openUI
import org.bukkit.entity.Player
import top.iseason.mmoforge.ui.ReFineUI


class UICommand(parent: CompositeCommand) : SimpleSubcommand(
    plugin = SimplePlugin.instance,
    name = "refine",
    adminOnly = false,
    parent = parent,
    usage = "${parent.name} gui",
    description = "打开强化界面",
) {
    override fun onCommand() {
        if (sender !is Player) return
        (sender as Player).openUI<ReFineUI> { }
    }
}

