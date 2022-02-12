/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/27 下午9:20
 *
 */

package top.iseason.rpgforgesystem

import com.entiv.core.hook.VaultEconomyHook
import com.entiv.core.menu.Button
import com.entiv.core.menu.Menu
import com.entiv.core.utils.sendErrorMessage
import io.lumine.mythic.lib.api.item.NBTItem
import net.kyori.adventure.text.Component
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import top.iseason.rpgforgesystem.uitls.*

class ForgeMenu : Menu(RPGForgeSystem.instance, "强化", 6, lock = true) {
    private var quality = 0
    private var forgeExp = 0
    private var limit = 0
    private var gold = 0.0
    private val placeHolder1 = ItemStack(Material.PAPER).apply {
        val meta = itemMeta
        meta.setDisplayName("${ChatColor.RED} 请放入可强化/精炼/突破的物品")
        itemMeta = meta
    }

    private val placeHolder2 = ItemStack(Material.PAPER).apply {
        val meta = itemMeta
        meta.setDisplayName("${ChatColor.RED} 请放入强化/精炼/突破 相应的材料")
        itemMeta = meta
    }

    init {
        setButton(Button(itemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            .apply {
                val meta = itemMeta
                meta.displayName(Component.text(""))
                itemMeta = meta
            }, slots = (0..53).toMutableList().apply {
            remove(19)
            remove(21)
            remove(23)
            remove(25)
        }.toIntArray(), onClick = {}, lock = true
        )
        )
        // 第一个物品槽
        setButton(Button(itemStack = ItemStack(Material.AIR), slots = intArrayOf(19), onClicked = {

            quality = 0
            inventory.setItem(28, placeHolder1)
            updateGold()
            val mmoItem = inventory.getItem(19) ?: return@Button
            if (mmoItem.type.isAir) return@Button
            val nbtItem = NBTItem.get(mmoItem)
            val quality = nbtItem.getInteger(Config.QUALITY_TAG)
            if (quality == 0) return@Button
            if (mmoItem.type.isTool()) return@Button
            this.quality = quality
            inventory.setItem(28, showInfo(nbtItem))

        }, lock = false))
        //第二个物品槽
        setButton(Button(itemStack = ItemStack(Material.AIR), slots = intArrayOf(21), onClicked = {
            this.forgeExp = 0
            inventory.setItem(30, placeHolder2)
            updateGold()
            val mmoItem = inventory.getItem(21) ?: return@Button
            if (mmoItem.type.isAir) return@Button
            val nbtItem = NBTItem.get(mmoItem)
            val exp = nbtItem.getInteger(Config.MATERIAL_FORGE_TAG)
            val limit = nbtItem.getInteger(Config.MATERIAL_LIMIT_TAG)
            inventory.setItem(30, showInfo(nbtItem))
            if (exp == 0 && limit == 0) return@Button
            this.forgeExp = exp
            this.limit = limit

        }, lock = false))
        //强化按钮
        setButton(Button(itemStack = ItemStack(Material.ANVIL).apply {
            val meta = itemMeta
            meta.setDisplayName("${ChatColor.RESET}点击强化")
            itemMeta = meta
        }, slots = intArrayOf(23), onClick = {

            if (this.quality == 0) return@Button
            var item1 = inventory.getItem(19) ?: return@Button
            val item2 = inventory.getItem(21) ?: return@Button
            val temp = item1
            if (this.forgeExp != 0) {
                item1 = item1.addExp(this.forgeExp * item1.amount)
            }
            if (this.limit != 0) {
                val data = item1.getRPGData(Config.LIMIT_TAG)
                val level = this.limit * item1.amount + data
                item1 = item1.addLimit(if (level > Config.MAX_LIMIT) Config.MAX_LIMIT - data else level - data)
            }
            val nbt1 = NBTItem.get(item1)
            val nbt2 = NBTItem.get(item2)
            if (nbt1.hasType() && nbt2.hasType() &&
                nbt1.getString("MMOITEMS_ITEM_ID") == nbt2.getString("MMOITEMS_ITEM_ID")
                && nbt1.hasTag(Config.QUALITY_TAG) && nbt2.hasTag(Config.QUALITY_TAG)
                && nbt1.getInteger(Config.QUALITY_TAG) == nbt2.getInteger(Config.QUALITY_TAG)
            ) {
                val refine1 = nbt1.getInteger(Config.REFINE_TAG)
                val refine2 = nbt2.getInteger(Config.REFINE_TAG)
                val level = refine1 + refine2 + 1
                item1 =
                    item1.addRefine(if (level > Config.MAX_REFINE) Config.MAX_REFINE - refine1 else level - refine1)
            }
            if (temp === item1) return@Button
            val player = it.whoClicked
            if (player !is Player) return@Button
            if (VaultEconomyHook.has(player, this.gold) == true) {
                val response = VaultEconomyHook.withdrawPlayer(player, this.gold)
                if (response?.type != EconomyResponse.ResponseType.SUCCESS) {
                    sendErrorMessage(player, "余额不足!")
                    return@Button
                }
            } else {
                sendErrorMessage(player, "余额不足!")
                return@Button
            }
            inventory.setItem(25, item1)
            reset()
            updateGold()

        }, lock = true))
        //结果槽
        setButton(Button(itemStack = ItemStack(Material.AIR), slots = intArrayOf(25), onClick = {

        }, lock = false))
        reset()
        updateGold()
    }

    private fun updateGold() {
        val item = ItemStack(Material.ANVIL)
        val item1 = inventory.getItem(19)
        val item2 = inventory.getItem(21)
        if (item1 == null || item2 == null || item1.type.isAir || item2.type.isAir) {
            with(item) {
                val meta = itemMeta
                meta.setDisplayName("${ChatColor.RED}请放入物品")
                itemMeta = meta
            }
            inventory.setItem(23, item)
            this.gold = 0.0
            return
        }
        val nbt1 = NBTItem.get(item1)
        val nbt2 = NBTItem.get(item2)
        val qu = nbt1.getInteger(Config.QUALITY_TAG)
        val quality = nbt2.getInteger(Config.QUALITY_TAG)
        val mForge = nbt2.getInteger(Config.MATERIAL_FORGE_TAG) * item2.amount
        val mLimit = nbt2.getInteger(Config.MATERIAL_LIMIT_TAG)
        val refine = nbt2.getInteger(Config.REFINE_TAG)
        if (qu == 0 || (quality == 0 && mForge == 0 && mLimit == 0 && refine == 0)) {
            with(item) {
                val meta = itemMeta
                meta.setDisplayName("${ChatColor.RED}请放入有效物品")
                itemMeta = meta
            }
            inventory.setItem(23, item)
            this.gold = 0.0
            return
        }
        val expression = Config.goldExpression.getString(qu.toString())?.replace("{forge}", mForge.toString())
            ?.replace("{limit}", mLimit.toString())
            ?.replace("{refine}", refine.toString()) ?: "0"
        val gold = parser.evaluate(expression)
        with(item) {
            val meta = itemMeta
            meta.lore = listOf("${ChatColor.GOLD}需要金币: ${ChatColor.AQUA} $gold")
            meta.setDisplayName("${ChatColor.GREEN}点击强化")
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            meta.addEnchant(Enchantment.DURABILITY, 3, true)
            itemMeta = meta
        }
        this.gold = gold
        inventory.setItem(23, item)
    }

    private fun reset() {
        inventory.setItem(21, null)
        inventory.setItem(19, null)
        inventory.setItem(28, placeHolder1)
        inventory.setItem(30, placeHolder2)
    }

    private fun showInfo(nbtItem: NBTItem) = ItemStack(Material.PAPER).apply {
        val meta = itemMeta
        val quality = nbtItem.getInteger(Config.QUALITY_TAG)
        val mForge = nbtItem.getInteger(Config.MATERIAL_FORGE_TAG)
        val mLimit = nbtItem.getInteger(Config.MATERIAL_LIMIT_TAG)
        meta.setDisplayName("${ChatColor.GOLD}物品信息")
        if (quality != 0)
            meta.lore = listOf(
                "${ChatColor.LIGHT_PURPLE}星级: ${ChatColor.YELLOW}${quality}",
                "${ChatColor.AQUA}强化等级: ${ChatColor.YELLOW}${nbtItem.getInteger(Config.FORGE_TAG)}",
                "${ChatColor.GREEN}精炼等级: ${ChatColor.YELLOW}${nbtItem.getInteger(Config.REFINE_TAG)}"
            )
        else meta.lore = listOf(
            "${ChatColor.AQUA}强化经验: ${ChatColor.YELLOW}${mForge * nbtItem.item.amount}",
            "${ChatColor.GREEN}突破经验: ${ChatColor.YELLOW}${mLimit * nbtItem.item.amount}"
        )
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addEnchant(Enchantment.DURABILITY, 3, true)
        itemMeta = meta
    }
}