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

import com.entiv.core.config.*
import com.entiv.core.debug.warn
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import java.lang.reflect.Field
import java.util.*

//配置保存路径
@FilePath("config.yml")
object MainConfig : SimpleYAMLConfig() {

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

    @Comment("", "物品星级标签，储存在物品NBT")
    @Key("quality_tag")
    var QUALITY_TAG = "SAKURA_QUALITY"

    @Comment("", "材料拥有的强化经验，储存在物品NBT")
    @Key("material_forge_tag")
    var MATERIAL_FORGE_TAG = "SAKURA_MATERIAL_FORGE"

    @Comment("", "材料拥有的突破等级，储存在物品NBT")
    @Key("material_limit_tag")
    var MATERIAL_LIMIT_TAG = "SAKURA_MATERIAL_LIMIT"

    @Comment("", "强化每级增加的属性，支持小数及范围")
    @Key("forge-map")
    var ForgeMap: MemorySection = YamlConfiguration().apply {
        set("ATTACK_DAMAGE", "3-5")
    }

    //实际使用的
    val forgeMap = mutableMapOf<DoubleStat, String>()

    @Comment("", "强化每级所需的经验公式")
    @Comment("{star}:武器星级 {forge}:强化等级 {limit}:突破等级 {refine}:精炼等级 ")
    @Key("forge-level-map")
    var ForgeLevelSection: MemorySection = YamlConfiguration().apply {
        set("10", "2*{star}+5*{forge}")
        set("20", "2*{star}+5.1*{forge}")
    }
    val forgeLevelMap = HashMap<Int, String>()

    @Comment("", "强化突破属性增加，支持小数、范围及百分比", "高等级覆盖低等级，不覆盖会继承")
    @Key("forge-limit-map")
    var ForgeLimitSection: MemorySection = YamlConfiguration().apply {
        createSection("1").set("ATTACK_DAMAGE", "1%")
        createSection("2").set("ATTACK_DAMAGE", "2%")
        createSection("3").set("ATTACK_DAMAGE", "3%")
        createSection("4").set("ATTACK_DAMAGE", "4%")
        createSection("5").set("ATTACK_DAMAGE", "5%")
    }
    val forgeLimitMap: MutableMap<Int, Map<DoubleStat, String>> = mutableMapOf()

    @Comment("", "精炼属性，支持小数、范围及百分比", "高等级覆盖低等级，不覆盖会继承")
    @Key("refine-map")
    var RefineSection: MemorySection = ForgeLimitSection
    val refineMap: MutableMap<Int, Map<DoubleStat, String>> = mutableMapOf()

    @Comment("", "金币公式")
    @Comment("{forge}:增加的强化经验 {limit}:增加的突破等级 {refine}:增加的精炼等级 ")
    @Key("gold-require-map")
    var goldForgeExpression: MemorySection = YamlConfiguration().apply {
        set("3", "{forge}*10+{limit}*200+{refine}*500")
        set("4", "({forge}*10+{limit}*200+{refine}*500)*1.5")
        set("5", "({forge}*10+{limit}*200+{refine}*500)*2.475")
    }

    override val onPreLoad: (ConfigState) -> Boolean = {
        //加载配置前调用
        true
    }
    override val onLoad: (ConfigState) -> Unit = {
        //加载配置后调用
        resetForge()
        reset(RefineSection, refineMap)
        reset(ForgeLimitSection, forgeLimitMap)
        restForgeLevelMap()
    }

    override val onPreSave: (ConfigState) -> Boolean = {
        //保存配置前调用
        true
    }
    override val onSave: (ConfigState) -> Unit = {
        //保存配置后调用
    }

    private fun resetForge() {
        forgeMap.clear()
        val clazz = ItemStats::class.java
        ForgeMap.getKeys(false).forEach {
            lateinit var declaredField: Field
            try {
                declaredField = clazz.getDeclaredField(it)
            } catch (e: Exception) {
                warn("Attribute no found! :${it}")
                return@forEach
            }
            val itemStat = declaredField.get(null)
            if (itemStat != null && itemStat is DoubleStat)
                forgeMap[itemStat] = ForgeMap.getString(it)!!
            else
                warn("Attribute is not a doubleData! :${it}")
        }
    }

    private fun reset(section: MemorySection, map: MutableMap<Int, Map<DoubleStat, String>>) {
        map.clear()
        val clazz = ItemStats::class.java
        for (key in section.getKeys(false)) {
            val level = try {
                key.toInt()
            } catch (e: NumberFormatException) {
                continue
            }
            val configurationSection = section.getConfigurationSection(key) ?: continue
            var mutableMapOf = mutableMapOf<DoubleStat, String>()
            configurationSection.getKeys(false).forEach {
                mutableMapOf = mutableMapOf()
                lateinit var declaredField: Field
                try {
                    declaredField = clazz.getDeclaredField(it)
                } catch (e: Exception) {
                    warn("Attribute no found! :${it}")
                    return@forEach
                }
                val itemStat = declaredField.get(null)
                val string = configurationSection.getString(it)
                if (itemStat != null && string != null && itemStat is DoubleStat)
                    mutableMapOf[itemStat] = string
                else
                    warn("Attribute is not a doubleData! :${it}")
            }
            map[level] = mutableMapOf
        }
    }

    fun restForgeLevelMap() {
        forgeLevelMap.clear()
        ForgeLevelSection.getKeys(false).forEach {
            forgeLevelMap[it.toInt()] = ForgeLevelSection.getString(it) ?: return@forEach
        }
    }

}
