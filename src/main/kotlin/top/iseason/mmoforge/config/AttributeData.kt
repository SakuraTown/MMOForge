/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/17 下午8:23
 *
 */

package top.iseason.mmoforge.config

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder
import net.Indyuce.mmoitems.stat.data.random.RandomStatData
import net.Indyuce.mmoitems.stat.data.type.Mergeable
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo
import net.Indyuce.mmoitems.stat.type.ItemStat

data class AttributeData(
    // 物品星级
    val star: Int,

    //精炼等级
    var refine: Int = 0,

    // 限制等级
    var limit: Int = 0,

    // 强化等级
    var forge: Int = 0,

    //总经验
    var totalExp: Double = 0.0,

    // 最大精炼等级
    var maxRefine: Int = MainConfig.MAX_REFINE,

    // 最大限制等级
    var maxLimit: Int = MainConfig.MAX_LIMIT,

    // 最大强化等级
    var maxForge: Int = MainConfig.MAX_LIMIT * MainConfig.LimitRate,

    // 精炼增益
    var refineGain: Map<Int, Map<ItemStat, UpgradeInfo>> = MainConfig.refineMap,

    // 突破增益
    var limitGain: Map<Int, Map<ItemStat, UpgradeInfo>> = MainConfig.forgeLimitMap,

    // 强化增益
    var forgeGain: Map<Int, Map<ItemStat, UpgradeInfo>> = MainConfig.forgeMap

) : StatData, Mergeable, RandomStatData {


    override fun merge(data: StatData?) {
        require(data is AttributeData) { "Cannot merge two different stat data types!" }
        require(data.star == star) { "Cannot merge two stat data with different star!" }
        //仅合并精炼等级
        refine += data.refine
    }

    //todo: 完善深拷贝，完成序列化与反序列化
    override fun cloneData(): AttributeData = copy()

    override fun isClear(): Boolean = refine == 0 && limit == 0 && forge == 0 && totalExp == 0.0
    override fun randomize(p0: MMOItemBuilder?) = this
}