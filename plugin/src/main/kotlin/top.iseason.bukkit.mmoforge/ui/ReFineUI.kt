/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午9:18
 *
 */

package top.iseason.bukkit.mmoforge.ui

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.hook.VaultHook.takeMoney
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import top.iseason.bukkit.mmoforge.uitls.refine
import top.iseason.bukkit.mmoforge.uitls.setName
import top.iseason.bukkittemplate.ui.container.ChestUI
import top.iseason.bukkittemplate.ui.slot.*
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.other.NumberUtils.toRoman
import top.iseason.bukkittemplate.utils.other.submit


class ReFineUI : ChestUI("物品精炼") {
    private var toolMMOForgeData: MMOForgeData? = null
    private var materialMMOForgeData: MMOForgeData? = null
    private var toolType: String? = null
    private var gold = 0.0


    init {//设置占位符
        setBackGround(Icon(ItemStack(Material.GRAY_STAINED_GLASS_PANE).applyMeta { setName(" ") }, 0))
    }

    private val refineButton =
        Button(ItemStack(Material.ANVIL).applyMeta { setName("${ChatColor.RED}无法精炼") }, index = 23).onClicked {
            if (gold == 0.0) return@onClicked
            if (!(it.whoClicked as Player).takeMoney(gold)) return@onClicked
            resetData()
            toolSlot.reset()
            materialSlot.reset()
            this.reset()
            resultSlot.outputAble(true)
        }.setup()

    private val toolSlot = IOSlot(19,
        ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setName("${ChatColor.RED} 请放入待精炼的物品")
        }).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        toolMMOForgeData = nbtItem.getForgeData() ?: return@inputFilter false
        toolType = nbtItem.getString("MMOITEMS_ITEM_ID")
        true
    }.onOutput {
        this@ReFineUI.reset()
    }.setup()

    private val materialSlot = IOSlot(21, ItemStack(Material.RED_STAINED_GLASS_PANE)
        .applyMeta { setName("${ChatColor.RED} 请放入相同的物品，将会被消耗") }
    ).inputFilter {
        if (toolType == null || toolMMOForgeData == null) return@inputFilter false
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        if (nbtItem.getString("MMOITEMS_ITEM_ID") != toolType) return@inputFilter false
        materialMMOForgeData = nbtItem.getForgeData() ?: return@inputFilter false
        true
    }.onInput {
        updateResult()
    }.onOutput {
        resultSlot.reset()
        updateResult()
    }.setup()

    private val resultSlot = IOSlot(25, null).inputAble(false).setup()

    private fun resetData() {
        toolMMOForgeData = null
        materialMMOForgeData = null
        toolType = null
        gold = 0.0
    }

    override fun reset() {
        resetData()
        val humanEntity = inventory.viewers.getOrNull(0)
        if (humanEntity != null)
            ejectItems(humanEntity)
        super.reset()
    }

    private fun updateResult() {
        val toolM = toolSlot.itemStack
        val materialM = materialSlot.itemStack
        if (materialM == null) {
            refineButton.reset()
            gold = 0.0
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
            val forgeData = mmoItem.getData(MMOForgeStat) as? MMOForgeData ?: return@submit
            var add = materialMMOForgeData!!.refine + 1
            add = if (forgeData.refine + add > forgeData.maxRefine) forgeData.maxRefine - forgeData.refine else add
            if (add == 0) return@submit
            mmoItem.refine(forgeData, add)
            forgeData.refine += add
            mmoItem.setData(MMOForgeStat, forgeData)
            val expression = MainConfig.goldForgeExpression.getString(forgeData.star.toString()) ?: return@submit
            gold = MainConfig.getValueByFormula(expression, forgeData.star, refine = add)
            refineButton.displayName = "${ChatColor.GREEN}点击精炼物品: ${ChatColor.GOLD}$gold ￥"
            refineButton.itemStack!!.applyMeta {
                addEnchant(Enchantment.BINDING_CURSE, 1, true)
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
            resultSlot.outputAble(false)
            val buildSilently = mmoItem.newBuilder().buildSilently()
            buildSilently.applyMeta {
                setName("$displayName ${forgeData.refine.toRoman()}")
            }
            resultSlot.itemStack = buildSilently
        }
    }

}
