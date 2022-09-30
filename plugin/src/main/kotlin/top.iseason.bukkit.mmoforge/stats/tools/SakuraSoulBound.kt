/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/5/1 下午3:14
 *
 */

package top.iseason.bukkit.mmoforge.stats.tools

import net.Indyuce.mmoitems.stat.type.BooleanStat
import org.bukkit.Material

object SakuraSoulBound : MMOAttribute(
    "SAKURA_SOUL_BOUND",
    Material.ENDER_EYE,
    "Sakura Soul Bound",
    "",
    arrayOf("灵魂绑定"),
    arrayOf("all")
) {
    override val stat = BooleanStat(mID, mMaterial, mName, mLore, mTypes)
}