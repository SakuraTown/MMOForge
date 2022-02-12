/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/2/5 下午11:09
 *
 */

package top.iseason.rpgforgesystem

import com.entiv.core.command.CommandBuilder
import org.bukkit.entity.Player


object AllCommand : CommandBuilder(RPGForgeSystem.instance, name = "rpg", onScope = {
    addSubCommand("gui") {
        onCommanded = {
            val sender1 = this.sender
            if (sender1 is Player)
                ForgeMenu().open(sender1)
        }
    }
})
