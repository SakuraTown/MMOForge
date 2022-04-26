/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/19 下午9:17
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/17 下午8:23
 *
 */

package top.iseason.mmoforge.stats

import io.lumine.mythic.utils.gson.JsonArray
import io.lumine.mythic.utils.gson.JsonObject
import io.lumine.mythic.utils.gson.JsonParser
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder
import net.Indyuce.mmoitems.stat.data.random.RandomStatData
import net.Indyuce.mmoitems.stat.data.type.Mergeable
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.ItemStat
import top.iseason.mmoforge.config.MainConfig

data class MMOForgeData(
    // 物品星级
    val star: Int,

    //精炼等级
    var refine: Int = 1,

    // 限制等级
    var limit: Int = 0,

    // 强化等级
    var forge: Int = 0,

    //当前强化经验
    var currentExp: Double = 0.0,

    // 最大精炼等级
    var maxRefine: Int = MainConfig.MAX_REFINE,

    // 最大限制等级
    var maxLimit: Int = MainConfig.MAX_LIMIT,

    // 最大强化等级
    var maxForge: Int = MainConfig.MAX_LIMIT * MainConfig.LimitRate,

    // 精炼增益
    var refineGain: ForgeParserMap = MainConfig.refineGain,

    // 突破增益
    var limitGain: ForgeParserMap = MainConfig.limitGain,

    // 强化增益
    var forgeGain: ForgeParserMap = MainConfig.forgeGain,

    var limitType: LinkedHashMap<Int, List<String>> = MainConfig.limitType
) : StatData, Mergeable, RandomStatData {
    /**
     * 获取当前最大强化等级
     */
    fun getCurrentMaxForge() = (limit + 1) * MainConfig.LimitRate

    override fun merge(data: StatData?) {
        require(data is MMOForgeData) { "Cannot merge two different stat data types!" }
        require(data.star == star) { "Cannot merge two stat data with different star!" }
        //仅合并精炼等级
        refine += data.refine
    }

    override fun cloneData(): MMOForgeData =
        copy(
            refineGain = LinkedHashMap(refineGain),
            limitGain = LinkedHashMap(limitGain),
            forgeGain = LinkedHashMap(forgeGain),
            limitType = LinkedHashMap(limitType),
        )

    fun toJson(): JsonObject = JsonObject().apply {
        addProperty("star", star)
        addProperty("refine", refine)
        addProperty("limit", limit)
        addProperty("forge", forge)
        addProperty("totalExp", currentExp)
        if (maxRefine != MainConfig.MAX_REFINE)
            addProperty("maxRefine", maxRefine)
        if (maxLimit != MainConfig.MAX_LIMIT)
            addProperty("maxLimit", maxLimit)
        if (maxForge != MainConfig.MAX_LIMIT * MainConfig.LimitRate)
            addProperty("maxForge", maxForge)
        if (refineGain != MainConfig.refineGain)
            add("gain-refine", refineGain.toJson())
        if (limitGain != MainConfig.limitGain)
            add("gain-limit", limitGain.toJson())
        if (forgeGain != MainConfig.forgeGain)
            add("gain-forge", forgeGain.toJson())
        if (limitType != MainConfig.limitType) {
            val jsonObject = JsonObject()
            limitType.forEach { (level, list) ->
                val temp = JsonArray()
                list.forEach { temp.add(it) }
                jsonObject.add(level.toString(), temp)
            }
            add("limit-type", jsonObject)
        }
    }


    /**
     * 获取当前强化等级升级所需要的经验
     */
    fun getForgeUpdateExp() =
        MainConfig.getValueByFormula(MainConfig.getForgeExpression(forge), star, forge, limit, refine)

    /**
     * 获取升级所需的经验
     */
    fun getRequireUpdateExp() = getForgeUpdateExp() - currentExp

    /**
     * 获取给与的经验可以升的强化等级、当前经验与剩余经验
     * @return 增加的等级${first} 、剩余的经验${second} 溢出的经验${third}
     */
    fun getLevelByExtraExp(exp: Double): Triple<Int, Double, Double> {
        var level = 0
        var remainExp = exp
        var current = currentExp
        val max = getCurrentMaxForge()
        while (true) {
            val fl = forge + level
            if (fl >= max) break
            //升级需要的经验
            val requireExp = MainConfig.getValueByFormula(
                MainConfig.getForgeExpression(fl),
                star,
                fl,
                limit,
                refine
            )
            //剩余的经验加上本身拥有的 是否能够升级
            if (remainExp + current < requireExp) {
                current += remainExp
                remainExp = 0.0
                break
            }
            remainExp -= (requireExp - current)
            current = 0.0
            level++
        }
        return Triple(level, current, remainExp)
    }

    override fun isClear(): Boolean = refine == 0 && limit == 0 && forge == 0 && currentExp == 0.0
    override fun randomize(p0: MMOItemBuilder?) = this

    companion object {
        fun fromString(string: String) = fromJson(JsonParser().parse(string).asJsonObject)

        fun fromJson(json: JsonObject): MMOForgeData {
            val star = json.get("star").asInt
            val attributeData = MMOForgeData(star)
            with(attributeData) {
                refine = json.get("refine").asInt
                limit = json.get("limit").asInt
                forge = json.get("forge").asInt
                currentExp = json.get("totalExp").asDouble
            }
            if (json.has("max-refine")) {
                attributeData.maxRefine = json.get("max-refine").asInt
            }
            if (json.has("max-limit")) {
                attributeData.maxLimit = json.get("max-limit").asInt
            }
            if (json.has("max-forge")) {
                attributeData.maxForge = json.get("max-forge").asInt
            }
            if (json.has("gain-refine")) {
                attributeData.refineGain = json.getAsJsonObject("gain-refine").toForgeMap()
            }
            if (json.has("gain-limit")) {
                attributeData.limitGain = json.getAsJsonObject("gain-limit").toForgeMap()
            }
            if (json.has("gain-forge")) {
                attributeData.forgeGain = json.getAsJsonObject("gain-forge").toForgeMap()
            }
            if (json.has("limit-type")) {
                val linkedHashMap = LinkedHashMap<Int, List<String>>()
                val jsonObject = json.get("limit-type").asJsonObject
                jsonObject.keySet().forEach {
                    val arrayListOf = arrayListOf<String>()
                    val jsonArray = jsonObject.getAsJsonArray(it)
                    jsonArray.forEach { s ->
                        arrayListOf.add(s.asString)
                    }
                    linkedHashMap[it.toInt()] = arrayListOf
                }
            }
            return attributeData
        }
    }

}

/**
 * 强化公式表转Json
 */
fun ForgeParserMap.toJson(): JsonObject {
    val jsonObject = JsonObject()
    forEach { (level, lMap) ->
        val temp = JsonObject()
        lMap.forEach { (statKey, info) ->
            temp.addProperty(statKey.id, info)
        }
        jsonObject.add(level.toString(), temp)
    }
    return jsonObject
}

/**
 * JsonObject转强化公式表
 */
fun JsonObject.toForgeMap(): ForgeParserMap {
    val map = LinkedHashMap<Int, LinkedHashMap<ItemStat, String>>()
    this.entrySet().forEach {
        val linkedHashMap = LinkedHashMap<ItemStat, String>()
        it.value.asJsonObject.entrySet().forEach { d ->
            val stat = MMOItems.plugin.stats.get(d.key)
            linkedHashMap[stat] = d.value.asString
        }
        map[it.key.toInt()] = linkedHashMap
    }
    return map
}
/**
 * 改个名字，太长了，因为主要是用于遍历，所以用LinkedHashMap
 */
typealias ForgeParserMap = LinkedHashMap<Int, LinkedHashMap<ItemStat, String>>