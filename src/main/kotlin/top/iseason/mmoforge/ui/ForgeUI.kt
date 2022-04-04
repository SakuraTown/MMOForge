/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/3 下午1:15
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/3/31 下午8:48
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.ui.*
import io.lumine.mythic.lib.api.item.NBTItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.uitls.*

class ForgeUI : ChestUI("强化/精炼/突破") {
    private var quality = 0
    private var gold = 0.0
    private var forgeExp = 0
    private var limit = 0
    private var refine = 0
    private var canForge = false

    init {//设置占位符
        setMultiSlots(Icon(Material.GRAY_STAINED_GLASS_PANE, " "), (1..53).toList())
    }

    private val toolInfo = Icon(Material.PAPER, "${ChatColor.RED}请放入可强化/精炼/突破的物品", index = 28).setUI(this)
    private val materialInfo = Icon(Material.PAPER, "${ChatColor.RED}请放入强化/精炼/突破 相应的材料", index = 30).setUI(this)
    private val forgeButton = Button(Material.ANVIL, "${ChatColor.RED}请放入物品和材料", index = 23).onClicked {
        if (!canForge) return@onClicked
        if ((it.whoClicked as? Player)?.takeMoney(gold) == false) return@onClicked
        var tool = toolSlot.itemStack!!
        if (forgeExp > 0) tool = tool.addExp(forgeExp)
        if (limit > 0) tool = tool.addLimit(limit)
        if (refine > 0) tool = tool.addRefine(refine)
        this@ForgeUI.reset()
        resultSlot.itemStack = tool

    }.setUI(this)
    private val toolSlot = IOSlot(19)
        .inputFilter {
            val nbtItem = NBTItem.get(it) ?: return@inputFilter false
            if (!nbtItem.hasType()) return@inputFilter false
            val quality = nbtItem.getInteger(MainConfig.QUALITY_TAG)
            quality > 0
        }.onInput {
            val nbtItem = NBTItem.get(itemStack)
            quality = nbtItem.getInteger(MainConfig.QUALITY_TAG)
            toolInfo.displayName = "${ChatColor.GOLD}物品信息 -> ${ChatColor.RESET}${itemStack?.itemMeta?.displayName}"
            toolInfo.lore = listOf(
                "${ChatColor.LIGHT_PURPLE}星级: ${ChatColor.YELLOW}${quality}",
                "${ChatColor.GREEN}精炼等级: ${ChatColor.YELLOW}${nbtItem.getInteger(MainConfig.REFINE_TAG)}",
                "${ChatColor.RED}突破次数: ${ChatColor.YELLOW}${nbtItem.getInteger(MainConfig.LIMIT_TAG)}",
                "${ChatColor.AQUA}强化等级: ${ChatColor.YELLOW}${nbtItem.getInteger(MainConfig.FORGE_TAG)}",
                "${ChatColor.YELLOW}强化经验: ${ChatColor.RED}${nbtItem.getInteger(MainConfig.FORGE_EXP_TAG)}${ChatColor.WHITE} / ${ChatColor.YELLOW}${nbtItem.getForgeUpdateExp()}"
            )
            updateResult()
        }.onOutput {
            quality = 0
            toolInfo.reset()
            updateResult()
        }.setUI(this)

    private val materialSlot = IOSlot(21)
        .inputFilter {
            val nbtItem = NBTItem.get(it) ?: return@inputFilter false
            val quality = nbtItem.getInteger(MainConfig.QUALITY_TAG)
            val tool = toolSlot.itemStack
            if (quality != 0 && tool != null) {
                val rptNbt = NBTItem.get(tool)
                if (rptNbt.getString("MMOITEMS_ITEM_ID") != nbtItem.getString("MMOITEMS_ITEM_ID") || rptNbt.getInteger(
                        MainConfig.QUALITY_TAG
                    ) != quality
                ) return@inputFilter false
            }
            val exp = nbtItem.getInteger(MainConfig.MATERIAL_FORGE_TAG)
            val limit = nbtItem.getInteger(MainConfig.MATERIAL_LIMIT_TAG)
            if (quality == 0 && exp == 0 && limit == 0) return@inputFilter false
            true
        }.onInput {
            val nbtItem = NBTItem.get(itemStack)
            val quality = nbtItem.getInteger(MainConfig.QUALITY_TAG)
            val mForge = nbtItem.getInteger(MainConfig.MATERIAL_FORGE_TAG) * nbtItem.item.amount
            val mlimit = nbtItem.getInteger(MainConfig.MATERIAL_LIMIT_TAG) * nbtItem.item.amount
            //是精炼
            if (quality != 0) {
                materialInfo.displayName =
                    "${ChatColor.GOLD}物品信息 -> ${ChatColor.RESET}${itemStack?.itemMeta?.displayName}"
                materialInfo.lore = listOf(
                    "${ChatColor.LIGHT_PURPLE}星级: ${ChatColor.YELLOW}${quality}",
                    "${ChatColor.GREEN}精炼等级: ${ChatColor.YELLOW}${nbtItem.getInteger(MainConfig.REFINE_TAG)}",
                    "${ChatColor.RED}突破次数: ${ChatColor.YELLOW}${nbtItem.getInteger(MainConfig.LIMIT_TAG)}",
                    "${ChatColor.AQUA}强化等级: ${ChatColor.YELLOW}${nbtItem.getInteger(MainConfig.FORGE_TAG)}",
                    "${ChatColor.YELLOW}强化经验: ${ChatColor.RED}${nbtItem.getInteger(MainConfig.FORGE_EXP_TAG)}${ChatColor.WHITE} / ${ChatColor.YELLOW}${nbtItem.getForgeUpdateExp()}"
                )
            } else {
                materialInfo.displayName =
                    "${ChatColor.GOLD}材料信息 -> ${ChatColor.RESET}${itemStack?.itemMeta?.displayName}"
                materialInfo.lore = listOf(
                    "${ChatColor.AQUA}强化经验: ${ChatColor.YELLOW}${mForge}",
                    "${ChatColor.GREEN}突破经验: ${ChatColor.YELLOW}${mlimit}"
                )
            }
            updateResult()
        }.onOutput {
            refine = 0
            forgeExp = 0
            limit = 0
            materialInfo.reset()
            updateResult()
        }.setUI(this)

    val resultSlot = IOSlot(25).inputAble(false).setUI(this)


    private fun updateResult() {
        val tool = toolSlot.itemStack
        val material = materialSlot.itemStack
        if (tool == null || material == null) {
            canForge = false
            gold = 0.0
            forgeButton.reset()
            return
        }
        val toolNbt = NBTItem.get(tool)!!
        val materialNbt = NBTItem.get(material)!!

        val toolQuantity = toolNbt.getInteger(MainConfig.QUALITY_TAG)

        val materialQuality = materialNbt.getInteger(MainConfig.QUALITY_TAG)

        val materialRefine =
            materialNbt.getInteger(MainConfig.REFINE_TAG) + if (toolQuantity == materialQuality) 1 else 0
        val refine1 = NBTItem.get(tool).getInteger(MainConfig.REFINE_TAG)
        val refineLevel = refine1 + materialRefine
        refine = if (refineLevel > MainConfig.MAX_REFINE) MainConfig.MAX_REFINE - refine1 else refineLevel - refine1
        /**
         * 需要判断能不能强化
         */
        val materialForge = materialNbt.getInteger(MainConfig.MATERIAL_FORGE_TAG) * material.amount

        val materialLimit = materialNbt.getInteger(MainConfig.MATERIAL_LIMIT_TAG) * material.amount
        val data = tool.getRPGData(MainConfig.LIMIT_TAG)
        val limitLevel = materialLimit + data
        limit = if (limitLevel > MainConfig.MAX_LIMIT) MainConfig.MAX_LIMIT - data else refineLevel - data

        val expression = MainConfig.goldForgeExpression.getString(toolQuantity.toString())
            ?.replace("{forge}", materialForge.toString())
            ?.replace("{limit}", limit.toString())
            ?.replace("{refine}", refine.toString()) ?: "0"
        this.gold = parser.evaluate(expression)
        with(forgeButton) {
            //精炼
            if (toolQuantity == materialQuality) {
                if (refine == 0) {
                    displayName = "${ChatColor.RED}已达最大精炼上限"
                    canForge = false
                    gold = 0.0
                    return
                }
                displayName = "${ChatColor.GREEN}点击${ChatColor.AQUA} 精炼 ${ChatColor.YELLOW}+$refine"
            } else if (materialForge > 0) {//强化或者突破
                forgeExp = materialForge
                displayName = "${ChatColor.GREEN}点击${ChatColor.AQUA} 强化 ${ChatColor.YELLOW}+$materialForge 点"
            } else if (materialLimit > 0) {
                if (limit == 0) {
                    displayName = "${ChatColor.RED}已达最大突破上限"
                    canForge = false
                    gold = 0.0
                    return
                }
                displayName = "${ChatColor.GREEN}点击${ChatColor.AQUA} 突破 ${ChatColor.YELLOW}+$limit"
            }
            lore = listOf("${ChatColor.GOLD}需要金币:${ChatColor.AQUA} $gold")
            itemMeta = itemMeta.apply {
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
                addEnchant(Enchantment.DURABILITY, 3, true)
            }
            canForge = true
        }
    }

    override fun reset() {
        canForge = false
        quality = 0
        gold = 0.0
        forgeExp = 0
        limit = 0
        refine = 0
        super.reset()
    }
}
/**
 * 仅仅为了区别
 */
typealias Icon = Button