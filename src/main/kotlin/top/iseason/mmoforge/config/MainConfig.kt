/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/3/26 上午12:19
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/20 下午8:50
 *
 */

package top.iseason.mmoforge.config

import com.entiv.core.common.toColor
import com.entiv.core.config.*
import com.entiv.core.utils.toRoman
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.stat.Enchants
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo
import net.Indyuce.mmoitems.stat.type.DoubleStat
import net.Indyuce.mmoitems.stat.type.ItemStat
import net.Indyuce.mmoitems.stat.type.Upgradable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import top.iseason.mmoforge.stats.ForgeParserMap
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.uitls.formatForgeString
import top.iseason.mmoforge.uitls.getProcessBar
import top.iseason.mmoforge.uitls.getStarCount
import top.iseason.mmoforge.uitls.kparser.ExpressionParser
import java.util.*

//配置保存路径
@FilePath("config.yml")
object MainConfig : SimpleYAMLConfig() {
    @Comment(
        "", "所有加成的格式：基础值为200",
        " +5 => 205",
        " -5 => 195",
        "  s5 => 5",
        " n5 => -5",
        " 5% => 10",
        "+5% => 210",
        "-5% => 190",
        " n5% => -10",
        " [3,5] => 203 - 205 的高斯分布区间"
    )
    @Key
    var AHEAD = "属性加成格式"

    @Comment("物品星级标签，储存在物品NBT")
    @Key("quality_tag")
    var QUALITY_TAG = "SAKURA_QUALITY"

    @Comment("强化标签，储存在物品NBT")
    @Key("foreg-tag")
    var FORGE_TAG = "SAKURA_FORGE"
    val ForgeUUID = UUID.fromString("9605eb27-aef7-477e-bb7e-d5075c82de85")

    @Comment("", "强化限制标签，储存在物品NBT")
    @Key("limit-tag")
    var LIMIT_TAG = "SAKURA_FORGE_LIMIT"
    val LimitUUID = UUID.fromString("aff6ef71-8963-4651-aa31-62782ba7e71f")

    @Comment("", "强化限制对应多少强化等级")
    @Key("limit-rate-tag")
    var LimitRate = 20

    @Comment("", "强化经验标签，储存在物品NBT")
    @Key("limit-exp-tag")
    var FORGE_EXP_TAG = "SAKURA_FORGE_EXP"

    @Comment("", "精炼标签，储存在物品NBT")
    @Key("refine-tag")
    var REFINE_TAG = "SAKURA_REFINE"
    val RefineUUID = UUID.fromString("6e6f2bb6-068e-481f-b913-1b80c2cf1dbb")

    @Comment("", "物品最大精炼等级，储存在物品NBT")
    @Key("max_refine_tag")
    var MAX_REFINE = 5

    @Comment("", "物品最大突破等级，储存在物品NBT")
    @Key("max_limit_tag")
    var MAX_LIMIT = 5


    @Comment("", "材料拥有的强化经验，储存在物品NBT")
    @Key("material_forge_tag")
    var MATERIAL_FORGE_TAG = "SAKURA_MATERIAL_FORGE"

    @Comment("", "材料拥有的突破等级，储存在物品NBT")
    @Key("material_limit_tag")
    var MATERIAL_LIMIT_TAG = "SAKURA_MATERIAL_LIMIT"


    @Comment("", "强化每级增加的属性，支持小数及范围")
    @Key("forge-map")
    var ForgeMap: MemorySection = YamlConfiguration().apply {
        createSection("1").set("ATTACK_DAMAGE", "1%")
        createSection("2").set("ATTACK_DAMAGE", "2%")
        createSection("3").set("ATTACK_DAMAGE", "3%")
        createSection("4").set("ATTACK_DAMAGE", "4%")
        createSection("5").set("ATTACK_DAMAGE", "5%")
    }

    //实际使用的
    var forgeGain: ForgeParserMap = LinkedHashMap()
        private set

    @Comment("", "强化每级所需的经验公式")
    @Comment("{star}:武器星级 {forge}:强化等级 {limit}:突破等级 {refine}:精炼等级 ")
    @Key("forge-level-map")
    var ForgeLevelSection: MemorySection = YamlConfiguration().apply {
        set("20", "2*{star}+5*{forge}")
        set("40", "2*{star}+5.1*{forge}")
    }
    val forgeLevelMap: LinkedHashMap<Int, String> = LinkedHashMap()

    /**
     * 根据目前强化等级获得对应的经验公式
     */
    fun getForgeExpression(level: Int): String {
        var formula = "2*{star}+5*{forge}"
        forgeLevelMap.forEach { (l, f) ->
            formula = f
            if (level <= l) return formula
        }
        return formula
    }

    /**
     * 根据公式填入数字并计算结果
     */
    fun getValueByFormula(
        formula: String,
        star: Number = 3,
        forge: Number = 0.0,
        limit: Number = 0,
        refine: Number = 0
    ): Double {
        val express = formula.replace("{star}", star.toString())
            .replace("{forge}", forge.toString())
            .replace("{limit}", limit.toString())
            .replace("{refine}", refine.toString())
        return try {
            ExpressionParser().evaluate(express)
        } catch (e: Exception) {
            0.0
        }
    }


    @Comment("", "强化突破属性增加，支持小数、范围及百分比", "高等级覆盖低等级，不覆盖会继承")
    @Key("limit-map")
    var ForgeLimitSection: MemorySection = YamlConfiguration().apply {
        createSection("1").set("ATTACK_DAMAGE", "1%")
        createSection("2").set("ATTACK_DAMAGE", "2%")
        createSection("3").set("ATTACK_DAMAGE", "3%")
        createSection("4").set("ATTACK_DAMAGE", "4%")
        createSection("5").set("ATTACK_DAMAGE", "5%")
    }

    @Comment("", "突破材料，如果某个等级没有声明将不能进行突破")
    @Key("limit-type-map")
    var LimitTypeSection: MemorySection = YamlConfiguration().apply {
        set("1", arrayListOf("STEEL_INGOT"))
        set("2", arrayListOf("STEEL_INGOT"))
        set("3", arrayListOf("STEEL_INGOT"))
        set("4", arrayListOf("STEEL_INGOT"))
        set("5", arrayListOf("STEEL_INGOT", "UNCOMMON_WEAPON_ESSENCE"))
    }

    var limitType: LinkedHashMap<Int, List<String>> = LinkedHashMap()
    var limitGain: ForgeParserMap = LinkedHashMap()
        private set

    @Comment("", "精炼属性，支持小数、范围及百分比", "高等级覆盖低等级，不覆盖会继承")
    @Key("refine-map")
    var RefineSection: MemorySection = YamlConfiguration().apply {
        createSection("1").set("ATTACK_DAMAGE", "1%")
        createSection("2").set("ATTACK_DAMAGE", "2%")
        createSection("3").set("ATTACK_DAMAGE", "3%")
        createSection("4").set("ATTACK_DAMAGE", "4%")
        createSection("5").set("ATTACK_DAMAGE", "5%")
    }
    var refineGain: ForgeParserMap = LinkedHashMap()
        private set

    @Comment("", "金币公式")
    @Comment("{forge}:增加的强化经验 {limit}:增加的突破等级 {refine}:增加的精炼等级 ")
    @Key("gold-require-map")
    var goldForgeExpression: MemorySection = YamlConfiguration().apply {
        set("3", "{forge}*1+{limit}*200+{refine}*500")
        set("4", "({forge}*1.1+{limit}*200+{refine}*500)*1.5")
        set("5", "({forge}*1.2+{limit}*200+{refine}*500)*2.475")
    }

    @Comment("", "数据在物品lore的显示")
    @Key("item-lore")
    var itemLore: List<String> = listOf(
        "",
        "&7■ &f星级：&5{star}",
        "&7■ &f精炼：&6{refine}",
        "&7■ &f突破：&a{limit}",
        "&7■ &f强化：&b{forge}",
        "&7■ 强化经验：&b{progress}"
    )

    /**
     * 由强化数据得lore
     */
    fun getItemLore(forgeData: MMOForgeData): List<String> {
        val lore = mutableListOf<String>()
        itemLore.forEach {
            lore.add(
                it.replace("{star}", getStarCount(forgeData.star))
                    .replace("{refine}", forgeData.refine.toRoman())
                    .replace("{limit}", forgeData.limit.toString())
                    .replace("{forge}", forgeData.forge.toString())
                    .replace(
                        "{progress}",
                        getProcessBar(10, forgeData.currentExp, forgeData.getForgeUpdateExp())
                    ).toColor()
            )
        }
        return lore
    }

    override val onPreLoad: (ConfigState) -> Boolean = {
        //加载配置前调用
        true
    }
    override val onLoad: (ConfigState) -> Unit = {
        //加载配置后调用
        forgeGain = getStatGain(ForgeMap)
        refineGain = getStatGain(RefineSection)
        limitGain = getStatGain(ForgeLimitSection)
        restForgeLevelMap()
        val linkedHashMap = LinkedHashMap<Int, List<String>>()
        LimitTypeSection.getKeys(false).forEach {
            linkedHashMap[it.toInt()] = LimitTypeSection.getStringList(it)
        }
        limitType = linkedHashMap
        MMOItems.plugin.upgrades.reload()
    }

    override val onPreSave: (ConfigState) -> Boolean = {
        //保存配置前调用
        true
    }
    override val onSave: (ConfigState) -> Unit = {
        //保存配置后调用
    }

    fun getStatGain(config: ConfigurationSection): ForgeParserMap {
        val mutableMapOf = LinkedHashMap<Int, LinkedHashMap<ItemStat, String>>()
        config.getKeys(false).forEach { levelStr ->
            val level = try {
                levelStr.toInt()
            } catch (e: Exception) {
                return@forEach
            }
            val section = config.getConfigurationSection(levelStr) ?: return@forEach
            val statWithUpgrade = LinkedHashMap<ItemStat, String>()
            for (statStr in section.getKeys(false)) {
                val stat = MMOItems.plugin.stats.get(statStr) ?: continue
                if (stat !is Upgradable) continue
                statWithUpgrade[stat] = section.getString(statStr)!!
            }
            mutableMapOf[level] = statWithUpgrade
        }
        return mutableMapOf
    }

    /**
     * 根据Stat的类型及提供的公式返回对应的UpgradeInfo
     */
    fun Upgradable.getUpgradeInfoByString(string: String): UpgradeInfo {
        if (this is Enchants) {
            return Enchants.EnchantUpgradeInfo.GetFrom(string.split(',').toList())
        }
        val areaValue = formatForgeString(string)
        var temp = string
        if (areaValue != null) {
            temp = string.replace(Regex("\\[(.+),(.+)]"), areaValue.toString())
        }
        return DoubleStat.DoubleUpgradeInfo.GetFrom(temp)
    }


    private fun restForgeLevelMap() {
        forgeLevelMap.clear()
        ForgeLevelSection.getKeys(false).forEach {
            forgeLevelMap[it.toInt()] = ForgeLevelSection.getString(it) ?: return@forEach
        }
    }


}

