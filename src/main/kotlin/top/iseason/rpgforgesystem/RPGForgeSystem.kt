/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午7:51
 *
 */

package top.iseason.rpgforgesystem


import com.entiv.core.plugin.SimplePlugin
import org.bukkit.Bukkit
import top.iseason.rpgforgesystem.configs.MainConfig

class RPGForgeSystem : SimplePlugin() {

    companion object {
        lateinit var instance: RPGForgeSystem
            private set
    }

    override fun onLoad() {
        instance = this
        super.onLoad()
//        debug = true
    }

    override fun onEnabled() {
        AllCommand
        Bukkit.getServer().pluginManager.registerEvents(EventListener, instance)
        MainConfig.init(instance)
    }

    override fun onDisabled() {

    }

}
