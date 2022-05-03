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

package top.iseason.mmoforge.stats.material

import org.bukkit.Material

import top.iseason.mmoforge.stats.tools.MMOAttribute
import java.io.File

object LimitLevel : MMOAttribute(
    "LIMIT_LEVEL",
    Material.EXPERIENCE_BOTTLE,
    "Limit level",
    "&7■ &f突破等级: &a#",
    arrayOf("可以突破物品"),
    arrayOf("material"),
    "materials${File.separatorChar}limit_level.yml"
) {

}