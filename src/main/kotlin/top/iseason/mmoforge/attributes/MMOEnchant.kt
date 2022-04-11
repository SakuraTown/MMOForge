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
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.Material
import org.bukkit.event.Listener
import top.iseason.mmoforge.MMOForge

/**
 * types see https://git.lumine.io/mythiccraft/mmoitems/-/blob/master/src/main/java/net/Indyuce/mmoitems/api/Type.java
 */
abstract class MMOEnchant(
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
    open val stat = EnchantStat()

    override val onLoad: (ConfigState) -> Unit = {
        MMOForge.instance.setStatLore(this)
        MMOForge.instance.setStatLoreFormat(this)
    }

    open inner class EnchantStat : DoubleStat(
        mID, mMaterial, mName,
        mLore, mTypes
    )
}
