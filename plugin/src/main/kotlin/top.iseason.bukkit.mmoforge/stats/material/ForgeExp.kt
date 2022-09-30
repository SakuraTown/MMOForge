/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午8:41
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午8:34
 *
 */

package top.iseason.bukkit.mmoforge.stats.material

import org.bukkit.Material
import top.iseason.bukkit.mmoforge.stats.tools.MMOAttribute
import java.io.File

object ForgeExp : MMOAttribute(
    "FORGE_EXP",
    Material.EXPERIENCE_BOTTLE,
    "Forge Exp",
    "&7■ &f强化经验: &a{value}",
    arrayOf("可以强化物品"),
    arrayOf("material"),
    "materials${File.separatorChar}forge_exp.yml"
)