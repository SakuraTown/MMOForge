/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午11:20
 *
 */

package top.iseason.bukkit.mmoforge.stats.tools

import io.lumine.mythic.lib.MythicLib
import net.Indyuce.mmoitems.api.UpgradeTemplate
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.Listener
import top.iseason.bukkit.mmoforge.MMOForge
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.Key

/**
 * types see https://git.lumine.io/mythiccraft/mmoitems/-/blob/master/src/main/java/net/Indyuce/mmoitems/api/Type.java
 */
abstract class MMOAttribute(
    val mID: String,
    val mMaterial: Material,
    val mName: String,
    format: String,
    val mLore: Array<String>,
    val mTypes: Array<String>,
    defaultPath: String = "enchants/${mID.lowercase()}.yml"
) : SimpleYAMLConfig(defaultPath = defaultPath), Listener {
    var loreKey: String = mID.lowercase().replace('_', '-')

    @Comment("格式字符，负责翻译lore")
    @Key("loreFormat")
    var loreFormat: String = format
    override fun onLoaded(section: ConfigurationSection) {
        if (MainConfig.updateLore) {
            MMOForge.setStatLore(this)
            MMOForge.setStatLoreFormat(this)
        }
    }

    /**
     * 用于将字符模板转换为字符
     */
    open fun formatLore(stat: DoubleStat, data: DoubleData): String {
        return DoubleStat.formatPath(
            stat.id, ItemStat.translate(stat.path),
            stat.moreIsBetter(), data.value * stat.multiplyWhenDisplaying()
        )
    }

    open val stat: ItemStat<*, *> = object : DoubleStat(mID, mMaterial, mName, mLore, mTypes) {

        override fun whenApplied(item: ItemStackBuilder, data: DoubleData) {
            val value = data.value
            if (value < 0 && !handleNegativeStats()) {
                return
            }
            var upgradeShift = 0.0
            if (UpgradeTemplate.isDisplayingUpgrades() && item.mmoItem.upgradeLevel != 0) {
                val hist = item.mmoItem.getStatHistory(this)
                if (hist != null) {
                    val uData = hist.recalculateUnupgraded() as DoubleData
                    upgradeShift = value - uData.value
                }
            }
            if (value != 0.0 || upgradeShift != 0.0) {
                var loreInsert: String? = formatLore(this, data)
                if (upgradeShift != 0.0) loreInsert += MythicLib.plugin.parseColors(
                    UpgradeTemplate.getUpgradeChangeSuffix(
                        if (upgradeShift * multiplyWhenDisplaying() >= 0.0) "+" else "" + MythicLib.plugin.mmoConfig.decimals.format(
                            upgradeShift * multiplyWhenDisplaying()
                        ),
                        !isGood(upgradeShift * multiplyWhenDisplaying())
                    )
                )
                item.lore.insert(path, loreInsert)
            }
            if (data.value != 0.0) {
                item.addItemTag(getAppliedNBT(data))
            }
        }
    }
}
