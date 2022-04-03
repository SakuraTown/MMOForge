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
import top.iseason.mmoforge.ForgeUI
import top.iseason.mmoforge.MMOForge


object AllCommand : CommandBuilder(MMOForge.instance, name = "rpg", onScope = {
    addSubCommand("gui") {
        onCommanded = {
            (sender as? Player)?.openUI<ForgeUI>()
        }
    }
})
