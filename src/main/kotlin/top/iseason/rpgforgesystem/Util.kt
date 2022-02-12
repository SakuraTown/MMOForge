/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/24 下午11:35
 *
 */

package top.iseason.rpgforgesystem

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun Material.isTool() = when (this) {

    Material.WOODEN_SHOVEL,
    Material.STONE_SHOVEL,
    Material.IRON_SHOVEL,
    Material.GOLDEN_SHOVEL,
    Material.DIAMOND_SHOVEL,
    Material.NETHERITE_SHOVEL,

    Material.WOODEN_PICKAXE,
    Material.STONE_PICKAXE,
    Material.IRON_PICKAXE,
    Material.GOLDEN_PICKAXE,
    Material.DIAMOND_PICKAXE,
    Material.NETHERITE_PICKAXE,

    Material.WOODEN_HOE,
    Material.STONE_HOE,
    Material.IRON_HOE,
    Material.GOLDEN_HOE,
    Material.DIAMOND_HOE,
    Material.NETHERITE_HOE,

    Material.WOODEN_AXE,
    Material.STONE_AXE,
    Material.IRON_AXE,
    Material.GOLDEN_AXE,
    Material.DIAMOND_AXE,
    Material.NETHERITE_AXE,

    Material.FISHING_ROD,
    Material.FLINT_AND_STEEL,
    Material.COMPASS,
    Material.CLOCK,
    Material.SHEARS,
    Material.SPYGLASS,
    Material.LEAD,
    Material.NAME_TAG -> true
    else -> false
}

fun ItemStack.reduce(amount: Int): Boolean {
    val amount1 = this.amount
    if (amount1 - amount <= 0) return false
    this.amount = this.amount - amount
    return true
}



