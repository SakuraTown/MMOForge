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
import top.iseason.mmoforge.command.UICommand
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.enchantment.EnchantmentStat
import top.iseason.mmoforge.enchantment.SilkTouch
import top.iseason.mmoforge.listener.EventListener

class MMOForge : SimplePlugin() {
    private val statLoreFormats = mutableListOf<EnchantmentStat>()

    companion object {
        lateinit var instance: MMOForge
            private set
    }

    override fun onLoad() {
        instance = this
        SilkTouch.register()
        setLoreFormat(statLoreFormats)
        setStatsLore(statLoreFormats)
        MMOItems.plugin.stats.register(SilkTouch)
        super.onLoad()
//        debug = true
    }

    override fun onEnabled() {
        val defaultCommand = DefaultCommand()
        defaultCommand.addSubcommand(UICommand(defaultCommand))
        defaultCommand.register()
        registerListener(EventListener)
        MainConfig.init(instance)

    }

    override fun onDisabled() {

    }

    fun setLoreFormat(stats: List<EnchantmentStat>) {
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

    private fun setStatsLore(stats: List<EnchantmentStat>) {
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

    fun EnchantmentStat.register() {
        if (!isEnabled) return
        MMOItems.plugin.stats.register(this)
        registerListener(this)
        statLoreFormats.add(this)
    }

    private fun registerStat(stat: EnchantmentStat) {
        if (!stat.isEnabled) return
        MMOItems.plugin.stats.register(stat)
        statLoreFormats.add(stat)
    }

}
