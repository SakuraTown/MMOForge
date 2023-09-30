/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午8:41
 *
 */

package top.iseason.bukkit.mmoforge.stats

import org.bukkit.Material
import top.iseason.bukkit.mmoforge.stats.tools.MMOAttribute

object RefineChance : MMOAttribute(
    "REFINE_CHANCE",
    Material.EXPERIENCE_BOTTLE,
    "Refine Chance",
    "&7■ &f精炼概率: &a{value} %",
    arrayOf("精炼成功率"),
    arrayOf("all"),
    "settings/refine_chance.yml"
)