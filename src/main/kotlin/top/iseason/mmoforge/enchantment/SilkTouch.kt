/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午9:42
 *
 */

package top.iseason.mmoforge.enchantment

import org.bukkit.Material

object SilkTouch : EnchantmentStat(
    "SILK_TOUCH",
    Material.IRON_PICKAXE,
    "Silk Touch",
    arrayOf("挖掘方块时有概率触发精准采集效果"),
    arrayOf("tool")
) {
    override val nbtKey: String = "MMOFORGE_SILK_TOUCH"
    override val loreKey = "silk-touch"
    override val loreFormat: String = "&7■ &f精准: &a# %"
}