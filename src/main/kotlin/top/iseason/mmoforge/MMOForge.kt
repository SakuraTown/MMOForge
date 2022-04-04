/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午7:51
 *
 */

package top.iseason.mmoforge


import com.entiv.core.command.DefaultCommand
import com.entiv.core.plugin.SimplePlugin
import org.bukkit.Bukkit
import top.iseason.mmoforge.command.UICommand
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.listener.EventListener

class MMOForge : SimplePlugin() {

    companion object {
        lateinit var instance: MMOForge
            private set
    }

    override fun onLoad() {
        instance = this
        super.onLoad()
//        debug = true
    }

    override fun onEnabled() {
        val defaultCommand = DefaultCommand()
        defaultCommand.addSubcommand(UICommand(defaultCommand))
        defaultCommand.register()
        Bukkit.getServer().pluginManager.registerEvents(EventListener, instance)
        MainConfig.init(instance)
    }

    override fun onDisabled() {

    }

}
