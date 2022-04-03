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

import com.entiv.core.command.CommandBuilder
import com.entiv.core.ui.openUI
import org.bukkit.entity.Player
import top.iseason.mmoforge.MMOForge
import top.iseason.mmoforge.ui.ForgeUI


object UICommand : CommandBuilder(MMOForge.instance, name = "MMOForge", aliases = listOf("mf"), onScope = {
    addSubCommand("gui") {
        onCommanded = {
            (sender as? Player)?.openUI<ForgeUI>()
        }
    }
})
