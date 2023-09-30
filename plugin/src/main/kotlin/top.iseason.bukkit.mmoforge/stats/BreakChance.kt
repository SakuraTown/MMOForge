/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午8:41
 *
 */

package top.iseason.bukkit.mmoforge.stats

import org.bukkit.Material
import top.iseason.bukkit.mmoforge.stats.tools.MMOAttribute

object BreakChance : MMOAttribute(
    "BREAK_CHANCE",
    Material.EXPERIENCE_BOTTLE,
    "Break Chance",
    "&7■ &f突破概率: &a{value} %",
    arrayOf("突破成功率"),
    arrayOf("all"),
    "settings/break_chance.yml"
)