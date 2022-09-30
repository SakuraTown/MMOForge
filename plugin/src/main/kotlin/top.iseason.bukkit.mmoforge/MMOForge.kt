/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/18 下午7:51
 *
 */

package top.iseason.bukkit.mmoforge


import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.ConfigFile
import net.Indyuce.mmoitems.manager.ConfigManager
import top.iseason.bukkit.mmoforge.command.mainCommands
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.hook.VaultHook
import top.iseason.bukkit.mmoforge.listener.MMOListener
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.stats.material.ForgeExp
import top.iseason.bukkit.mmoforge.stats.material.LimitLevel
import top.iseason.bukkit.mmoforge.stats.tools.*
import top.iseason.bukkittemplate.KotlinPlugin
import top.iseason.bukkittemplate.command.CommandHandler
import top.iseason.bukkittemplate.debug.info
import top.iseason.bukkittemplate.ui.UIListener
import top.iseason.bukkittemplate.utils.bukkit.EventUtils.register

object MMOForge : KotlinPlugin() {
    private val statLoreFormats = mutableListOf<MMOAttribute>()

    override fun onAsyncEnable() {
        VaultHook.checkHooked()
        mainCommands()
        CommandHandler.updateCommands()
        MainConfig.load(false)
        registerStats()
        setStatsLoreFormat(statLoreFormats)
        setStatsLore(statLoreFormats)
        UIListener.register()
        MMOListener.register()

        info("&a插件已启用")
    }

    override fun onDisable() {
        info("&e插件已注销")
    }


    /**
     * 注册所有属性
     */
    private fun registerStats() {
        MMOItems.plugin.stats.register(MMOForgeStat)
        ForgeExp.reg()
        LimitLevel.reg()
        SilkTouch.reg()
        FortuneOre.reg()
        VeinOre.reg()
        VeinLog.reg()
        ScopeMiner.reg()
        SmeltOre.reg()
        SmeltShovel.reg()
        Harvester.reg()
        AutoTorch.reg()
        SpeedUp.reg()
        SakuraSoulBound.reg()
    }


    private fun setStatsLoreFormat(stats: List<MMOAttribute>) {
        val declaredField = ConfigManager::class.java.getDeclaredField("loreFormat")
        declaredField.isAccessible = true
        val config = declaredField.get(MMOItems.plugin.language) as ConfigFile
        val list = config.config.getStringList("lore-format").toMutableSet()
        for (stat in stats) {
            val loreKey = "#${stat.loreKey}#"
            list.add(loreKey)
        }
        list.add("#${MMOForgeStat.path}#")
        config.config.set("lore-format", list.toList())
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

    private fun MMOAttribute.reg() {
        registerStat(this)
        load(false)
        register()
    }

    private fun registerStat(mmoEnchant: MMOAttribute) {
        if (!mmoEnchant.stat.isEnabled) return
        statLoreFormats.add(mmoEnchant)
        MMOItems.plugin.stats.register(mmoEnchant.stat)
    }

}
