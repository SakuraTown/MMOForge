/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午8:41
 *
 */

package top.iseason.bukkit.mmoforge.stats

import org.bukkit.Material
import top.iseason.bukkit.mmoforge.stats.tools.MMOAttribute

object ForgeChance : MMOAttribute(
    "FORGE_CHANCE",
    Material.EXPERIENCE_BOTTLE,
    "Forge Chance",
    "&7■ &f强化概率: &a{value} %",
    arrayOf("强化成功率"),
    arrayOf("all"),
    "settings/forge_chance.yml"
)