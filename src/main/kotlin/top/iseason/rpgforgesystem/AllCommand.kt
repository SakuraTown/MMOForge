/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/2/5 下午11:09
 *
 */

package top.iseason.rpgforgesystem

import com.entiv.core.command.CommandBuilder
import com.entiv.core.ui.openUI
import org.bukkit.entity.Player


object AllCommand : CommandBuilder(RPGForgeSystem.instance, name = "rpg", onScope = {
    addSubCommand("gui") {
        onCommanded = {
            (sender as? Player)?.openUI<ForgeUI>()
        }
    }
})
