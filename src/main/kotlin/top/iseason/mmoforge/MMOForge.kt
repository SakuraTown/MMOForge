/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午7:51
 *
 */

package top.iseason.mmoforge


import com.entiv.core.command.DefaultCommand
import com.entiv.core.plugin.SimplePlugin
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.ConfigFile
import net.Indyuce.mmoitems.manager.ConfigManager
import top.iseason.mmoforge.command.BreakThroughCommand
import top.iseason.mmoforge.command.ForgeCommand
import top.iseason.mmoforge.command.RefineCommand
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.listener.EventListener
import top.iseason.mmoforge.stats.ForgeStat
import top.iseason.mmoforge.stats.material.ForgeExp
import top.iseason.mmoforge.stats.material.LimitLevel
import top.iseason.mmoforge.stats.tools.*

class MMOForge : SimplePlugin() {
    private val statLoreFormats = mutableListOf<MMOAttribute>()

    companion object {
        lateinit var instance: MMOForge
            private set
    }

    override fun onLoad() {
        instance = this
        registerStats()
        super.onLoad()
//        debug = true
    }


    override fun onEnabled() {
        val defaultCommand = DefaultCommand()
        defaultCommand.aliases = listOf("mf")
        RefineCommand(defaultCommand).register()
        ForgeCommand(defaultCommand).register()
        BreakThroughCommand(defaultCommand).register()
        defaultCommand.register()
        registerListener(EventListener)
        MainConfig.init(instance)
        setStatsLoreFormat(statLoreFormats)
        setStatsLore(statLoreFormats)
        registerStats(statLoreFormats)
    }

    override fun reload() {

    }

    override fun onDisabled() {
//        println(1)
    }

    /**
     * 注册所有属性
     */
    private fun registerStats() {
        MMOItems.plugin.stats.register(ForgeStat)
        ForgeExp.register()
        LimitLevel.register()
        SilkTouch.register()
        FortuneOre.register()
        VeinOre.register()
        VeinLog.register()
        ScopeMiner.register()
        SmeltOre.register()
        SmeltShovel.register()
        Harvester.register()
        AutoTorch.register()
        SpeedUp.register()
    }


    private fun setStatsLoreFormat(stats: List<MMOAttribute>) {
        val declaredField = ConfigManager::class.java.getDeclaredField("loreFormat")
        declaredField.isAccessible = true
        val config = declaredField.get(MMOItems.plugin.language) as ConfigFile
        val list = config.config.getStringList("lore-format")
        for (stat in stats) {
            val loreKey = "#${stat.loreKey}#"
            if (list.contains(loreKey)) continue
            list.add(loreKey)
        }
        config.config.set("lore-format", list)
        config.save()
        declaredField.isAccessible = false
    }

    fun setStatLoreFormat(stat: MMOAttribute) {
        val declaredField = ConfigManager::class.java.getDeclaredField("loreFormat")
        declaredField.isAccessible = true
        val config = declaredField.get(MMOItems.plugin.language) as ConfigFile
        val list = config.config.getStringList("lore-format")
        val loreKey = "#${stat.loreKey}#"
        if (list.contains(loreKey)) return
        list.add(loreKey)
        config.config.set("lore-format", list)
        config.save()
        declaredField.isAccessible = false
    }

    private fun setStatsLore(stats: List<MMOAttribute>) {
        val declaredField = ConfigManager::class.java.getDeclaredField("stats")
        declaredField.isAccessible = true
        val configFile = declaredField.get(MMOItems.plugin.language) as ConfigFile
        val config = configFile.config
        for (stat in stats) {
            config.set(stat.loreKey, stat.loreFormat)
        }
        configFile.save()
        declaredField.isAccessible = false
    }

    fun setStatLore(stat: MMOAttribute) {
        val declaredField = ConfigManager::class.java.getDeclaredField("stats")
        declaredField.isAccessible = true
        val configFile = declaredField.get(MMOItems.plugin.language) as ConfigFile
        val config = configFile.config
        config.set(stat.loreKey, stat.loreFormat)
        configFile.save()
        declaredField.isAccessible = false
    }

    private fun MMOAttribute.register() {
        registerStat(this)
    }

    private fun registerStat(mmoEnchant: MMOAttribute) {
        if (!mmoEnchant.stat.isEnabled) return
        statLoreFormats.add(mmoEnchant)
        MMOItems.plugin.stats.register(mmoEnchant.stat)
    }

    private fun registerStats(mmoEnchants: List<MMOAttribute>) {
        for (mmoEnchant in mmoEnchants) {
            mmoEnchant.init(this)
            registerListener(mmoEnchant)
        }
    }
}
