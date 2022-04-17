/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午11:20
 *
 */

package top.iseason.mmoforge.attributes

import com.entiv.core.config.Comment
import com.entiv.core.config.ConfigState
import com.entiv.core.config.Key
import com.entiv.core.config.SimpleYAMLConfig
import io.lumine.mythic.lib.MythicLib
import net.Indyuce.mmoitems.api.UpgradeTemplate
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.Material
import org.bukkit.event.Listener
import top.iseason.mmoforge.MMOForge

/**
 * types see https://git.lumine.io/mythiccraft/mmoitems/-/blob/master/src/main/java/net/Indyuce/mmoitems/api/Type.java
 */
abstract class MMOAttribute(
    val mID: String,
    val mMaterial: Material,
    val mName: String,
    val format: String,
    val mLore: Array<out String>,
    val mTypes: Array<out String>
) : SimpleYAMLConfig(defaultPath = "enchants/${mID.lowercase()}.yml"), Listener {

    @Comment("识别标签，储存在物品NBT")
    @Key("nbtKey")
    var nbtKey: String = "MMOFORGE_${mID}"

    @Comment("lore的占位符，将由格式字符格式化")
    @Key("loreKey")
    var loreKey: String = mID.lowercase().replace('_', '-')

    @Comment("格式字符，负责翻译lore")
    @Key("loreFormat")
    var loreFormat: String = format

    override val onLoad: (ConfigState) -> Unit = {
        MMOForge.instance.setStatLore(this)
        MMOForge.instance.setStatLoreFormat(this)
    }

    /**
     * 用于将字符模板转换为字符
     */
    open val loreAction: DoubleStat.(DoubleData) -> String = {
        DoubleStat.formatPath(
            ItemStat.translate(path),
            moreIsBetter(),
            it.value * multiplyWhenDisplaying()
        )
    }

    open val stat: ItemStat = object : DoubleStat(mID, mMaterial, mName, mLore, mTypes) {
        override fun whenApplied(item: ItemStackBuilder, data: StatData) {
            val value = (data as DoubleData).value
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
                var loreInsert: String? = loreAction.invoke(this, data)
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
