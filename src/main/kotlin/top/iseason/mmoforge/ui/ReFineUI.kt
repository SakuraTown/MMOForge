/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午9:18
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.common.submit
import com.entiv.core.ui.*
import com.entiv.core.utils.bukkit.applyMeta
import com.entiv.core.utils.bukkit.giveItems
import com.entiv.core.utils.bukkit.takeMoney
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.stats.ForgeStat
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.uitls.getForgeData
import top.iseason.mmoforge.uitls.refine


class ReFineUI : ChestUI("物品精炼") {
    private var toolMMOForgeData: MMOForgeData? = null
    private var materialMMOForgeData: MMOForgeData? = null
    private var toolType: String? = null
    private var gold = 0.0


    init {//设置占位符
        setBackGround(Icon(ItemStack(Material.GRAY_STAINED_GLASS_PANE).applyMeta { setDisplayName(" ") }, 0))
    }

    private val refineButton =
        Button(ItemStack(Material.ANVIL).applyMeta { setDisplayName("${ChatColor.RED}无法精炼") }, index = 23).onClicked {
            if (gold == 0.0) return@onClicked
            if (!(it.whoClicked as Player).takeMoney(gold)) return@onClicked
            toolMMOForgeData = null
            materialMMOForgeData = null
            toolType = null
            gold = 0.0
            toolSlot.reset()
            materialSlot.reset()
            reset()
            resultSlot.outputAble(true)
        }.setUI(this)
    private val toolSlot = IOSlot(19,
        ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setDisplayName("${ChatColor.RED} 请放入待精炼的物品")
        }).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        toolMMOForgeData = nbtItem.getForgeData() ?: return@inputFilter false
        toolType = nbtItem.getString("MMOITEMS_ITEM_ID")
        true
    }.onInput {
        updateResult()
    }.onOutput {

        updateResult()
    }.setUI(this)

    private val materialSlot = IOSlot(21,
        ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setDisplayName("${ChatColor.RED} 请放入相同的物品，将会被消耗")
        }).inputFilter {
        if (toolType == null) return@inputFilter false
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        if (nbtItem.getString("MMOITEMS_ITEM_ID") != toolType) return@inputFilter false
        materialMMOForgeData = nbtItem.getForgeData() ?: return@inputFilter false
        true
    }.onInput {
        updateResult()
    }.onOutput {
        updateResult()
    }.setUI(this)

    private val resultSlot = IOSlot(25).inputAble(false).setUI(this)

    private fun updateResult() {

        val toolM = toolSlot.itemStack
        val materialM = materialSlot.itemStack
        if (toolM == null || materialM == null) {
            val itemStack = resultSlot.itemStack
            if (itemStack != null && resultSlot.output(resultSlot, itemStack))
                getViewers().getOrNull(0)?.giveItems(itemStack)
            resultSlot.reset()
            refineButton.reset()
            gold = 0.0
            if (toolM == null) {
                toolMMOForgeData = null
                toolType = null
            }
            return
        }
        submit(async = true) {
            val toolNBT = NBTItem.get(toolM)
            if (!toolNBT.hasType()) {
                resultSlot.reset()
                refineButton.reset()
                gold = 0.0
                return@submit
            }
            val mmoItem = LiveMMOItem(toolNBT)
            val forgeData = NBTItem.get(toolSlot.itemStack).getForgeData() ?: return@submit
            var add = materialMMOForgeData!!.refine + 1
            add = if (forgeData.refine + add > forgeData.maxRefine) forgeData.maxRefine - forgeData.refine else add
            if (add == 0) return@submit
            mmoItem.refine(forgeData, add)
            forgeData.refine += add
            mmoItem.setData(ForgeStat, forgeData)
            val expression = MainConfig.goldForgeExpression.getString(forgeData.star.toString()) ?: return@submit
            gold = MainConfig.getValueByFormula(expression, forgeData.star, refine = add)
            refineButton.displayName = "${ChatColor.GREEN}点击精炼武器: ${ChatColor.GOLD}$gold ￥"
            refineButton.itemStack!!.applyMeta {
                addEnchant(Enchantment.BINDING_CURSE, 1, true)
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
            resultSlot.itemStack = mmoItem.newBuilder().buildNBT().toItem()
            resultSlot.outputAble(false)
        }
    }

}
