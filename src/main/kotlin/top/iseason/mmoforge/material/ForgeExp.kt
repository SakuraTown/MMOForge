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

package top.iseason.mmoforge.material

import org.bukkit.Material
import top.iseason.mmoforge.attributes.MMOAttribute

object ForgeExp : MMOAttribute(
    "FORGE_EXP",
    Material.EXPERIENCE_BOTTLE,
    "Forge Exp",
    "&7■ &f强化经验: &a#",
    arrayOf("可以强化物品"),
    arrayOf("material"),
    "materials/forge_exp.yml"
) {

}