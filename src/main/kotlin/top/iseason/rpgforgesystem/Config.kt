/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/20 下午8:50
 *
 */

package top.iseason.rpgforgesystem

import com.entiv.core.config.*
import com.entiv.core.debug.SimpleLogger
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import java.lang.reflect.Field
import java.util.*

//配置保存路径
@FilePath("config.yml")
object Config : SimpleYAMLConfig() {

    @Comment("强化标签，储存在物品NBT")
    @Key("foreg-tag")
    var FORGE_TAG = "SAKURA_FORGE"
    val ForgeUUID = UUID.fromString("9605eb27-aef7-477e-bb7e-d5075c82de85")

    @Comment("", "强化限制标签，储存在物品NBT")
    @Key("limit-tag")
    var LIMIT_TAG = "SAKURA_FORGE_LIMIT"
    val LimitUUID = UUID.fromString("aff6ef71-8963-4651-aa31-62782ba7e71f")

    @Comment("", "精炼标签，储存在物品NBT")
    @Key("refine-tag")
    var REFINE_TAG = "SAKURA_REFINE"
    val RefineUUID = UUID.fromString("6e6f2bb6-068e-481f-b913-1b80c2cf1dbb")

    @Comment("", "物品星级标签，储存在物品NBT")
    @Key("quality_tag")
    var QUALITY_TAG = "SAKURA_QUALITY"

    @Comment("", "强化属性，支持小数及范围")
    @Key("forge-map")
    var ForgeMap = listOf("ATTACK_DAMAGE 3-5")

    //实际使用的
    val forgeMap = mutableMapOf<DoubleStat, String>()

    @Comment("", "强化突破属性增加，支持小数及百分比")
    @Key("forge-limit-map")
    var ForgeLimitSection: MemorySection = YamlConfiguration().apply {
        createSection("1").set("ATTACK_DAMAGE", "1%")
        createSection("2").set("ATTACK_DAMAGE", "2%")
        createSection("3").set("ATTACK_DAMAGE", "3%")
        createSection("4").set("ATTACK_DAMAGE", "4%")
        createSection("5").set("ATTACK_DAMAGE", "5%")
    }
    val forgeLimitMap: MutableMap<Int, Map<DoubleStat, String>> = mutableMapOf()


    @Comment("", "精炼属性，支持小数及百分比")
    @Key("refine-map")
    var RefineSection: MemorySection = YamlConfiguration().apply {
        createSection("1").set("ATTACK_DAMAGE", "1%")
        createSection("2").set("ATTACK_DAMAGE", "2%")
        createSection("3").set("ATTACK_DAMAGE", "3%")
        createSection("4").set("ATTACK_DAMAGE", "4%")
        createSection("5").set("ATTACK_DAMAGE", "5%")
    }
    val refineMap: MutableMap<Int, Map<DoubleStat, String>> = mutableMapOf()


    override val onPreLoad: (ConfigState) -> Boolean = {
        //加载配置前调用
        true
    }
    override val onLoad: (ConfigState) -> Unit = {
        //加载配置后调用
        resetForge()
        reset(RefineSection, refineMap)
        reset(ForgeLimitSection, forgeLimitMap)
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
        ForgeMap.forEach {
            val split = it.split(" ")
            if (split.size != 2) return@forEach
            lateinit var declaredField: Field
            try {
                declaredField = clazz.getDeclaredField(split[0])
            } catch (e: Exception) {
                SimpleLogger.warn("Attribute no found! :${split[0]}")
                return@forEach
            }
            val itemStat = declaredField.get(null)
            if (itemStat != null && itemStat is DoubleStat)
                forgeMap[itemStat] = split[1]
            else
                SimpleLogger.warn("Attribute is not a doubleData! :${split[0]}")
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
                    SimpleLogger.warn("Attribute no found! :${it}")
                    return@forEach
                }
                val itemStat = declaredField.get(null)
                val string = configurationSection.getString(it)
                if (itemStat != null && string != null && itemStat is DoubleStat)
                    mutableMapOf[itemStat] = string
                else
                    SimpleLogger.warn("Attribute is not a doubleData! :${it}")
            }
            map[level] = mutableMapOf
        }
    }

}
