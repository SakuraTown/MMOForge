/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午7:51
 *
 */

package top.iseason.rpgforgesystem


import com.entiv.core.plugin.SimplePlugin
import org.bukkit.Bukkit

class RPGForgeSystem : SimplePlugin() {

    companion object {
        lateinit var instance: RPGForgeSystem
            private set
    }

    override fun onLoad() {
        instance = this
        super.onLoad()
        debug = true
    }

    override fun onEnabled() {
        MainCommand().register()
        Bukkit.getServer().pluginManager.registerEvents(EventListener, instance)
        Config.init(instance)
    }

    override fun onDisabled() {

    }

}
