/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午9:18
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.ui.*
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.stats.ForgeData
import top.iseason.mmoforge.stats.ForgeStat
import top.iseason.mmoforge.uitls.getForgeData
import top.iseason.mmoforge.uitls.kparser.ExpressionParser
import top.iseason.mmoforge.uitls.takeMoney

class ReFineUI : ChestUI("物品精炼") {
    private var toolForgeData: ForgeData? = null
    private var materialForgeData: ForgeData? = null
    private var toolType: String? = null
    private var gold = 0.0

    init {//设置占位符
        setMultiSlots(Icon(Material.GRAY_STAINED_GLASS_PANE, " "), (1..53).toList())
    }

    private val toolInfo = Icon(Material.PAPER, "${ChatColor.RED}请放入待精炼的物品", index = 28).setUI(this)
    private val materialInfo = Icon(Material.PAPER, "${ChatColor.RED}请放入相同的物品", index = 30).setUI(this)
    private val forgeButton = Button(Material.ANVIL, "${ChatColor.RED}无法精炼", index = 23).onClicked {
        if (gold == 0.0) return@onClicked
        if (!(it.whoClicked as Player).takeMoney(gold)) return@onClicked
        toolForgeData = null
        materialForgeData = null
        toolType = null
        gold = 0.0
        toolSlot.reset()
        toolInfo.reset()
        materialSlot.reset()
        materialInfo.reset()
        reset()
        resultSlot.outputAble(true)
    }.setUI(this)

    private val toolSlot = IOSlot(19).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        toolForgeData = nbtItem.getForgeData() ?: return@inputFilter false
        toolType = nbtItem.getString("MMOITEMS_ITEM_ID")
        true
    }.onInput {
        val data = toolForgeData ?: return@onInput
        showRefineInfo(toolInfo, itemStack!!, data)
        updateResult()
    }.onOutput {
        toolForgeData = null
        toolType = null
        toolInfo.reset()
        updateResult()
    }.setUI(this)

    private val materialSlot = IOSlot(21)
        .inputFilter {
            if (toolType == null) return@inputFilter false
            val nbtItem = NBTItem.get(it) ?: return@inputFilter false
            if (nbtItem.getString("MMOITEMS_ITEM_ID") != toolType) return@inputFilter false
            materialForgeData = nbtItem.getForgeData() ?: return@inputFilter false
            true
        }.onInput {
            val data = materialForgeData ?: return@onInput
            showRefineInfo(materialInfo, itemStack!!, data)
            updateResult()
        }.onOutput {
            materialForgeData = null
            materialInfo.reset()
            updateResult()
        }.setUI(this)

    private val resultSlot = IOSlot(25).inputAble(false).setUI(this)

    private fun showRefineInfo(button: Button, itemStack: ItemStack, data: ForgeData) {
        button.displayName = "${ChatColor.GOLD}物品信息 -> ${ChatColor.RESET}${itemStack.itemMeta.displayName}"
        button.lore = listOf(
            "${ChatColor.LIGHT_PURPLE}星级: ${ChatColor.YELLOW}${data.star}",
            "${ChatColor.GREEN}精炼等级: ${ChatColor.YELLOW}${data.refine}",
            "${ChatColor.RED}突破次数: ${ChatColor.YELLOW}${data.limit}",
            "${ChatColor.AQUA}强化等级: ${ChatColor.YELLOW}${data.forge}",
            "${ChatColor.YELLOW}强化经验: ${ChatColor.RED}${data.totalExp}"
        )
    }

    private fun updateResult() {
        if (toolSlot.itemStack == null || materialSlot.itemStack == null) {
            resultSlot.reset()
            forgeButton.reset()
            gold = 0.0
            return
        }
        val mmoItem = LiveMMOItem(toolSlot.itemStack)
        val forgeData = NBTItem.get(toolSlot.itemStack).getForgeData() ?: return
        val add = materialForgeData!!.refine + 1
        forgeData.refine += add
        mmoItem.setData(ForgeStat, forgeData)
        //todo: 应用精炼增值
        val expression = MainConfig.goldForgeExpression.getString(forgeData.star.toString()) ?: return
        val express = expression.replace("{forge}", "0").replace("{limit}", "0").replace("{refine}", add.toString())
        gold = ExpressionParser().evaluate(express)
        forgeButton.displayName = "${ChatColor.GREEN}点击精炼武器: ${ChatColor.GOLD}$gold ￥"
        resultSlot.itemStack = mmoItem.newBuilder().buildNBT().toItem()
        resultSlot.outputAble(false)
    }

}