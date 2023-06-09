/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/26 下午10:20
 *
 */

package top.iseason.bukkit.mmoforge.ui

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.Type
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.type.NameData
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import top.iseason.bukkit.mmoforge.config.BreakUIConfig
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.hook.PAPIHook
import top.iseason.bukkit.mmoforge.hook.VaultHook.takeMoney
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.uitls.breakthrough
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import top.iseason.bukkittemplate.ui.container.ChestUI
import top.iseason.bukkittemplate.ui.slot.*
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.checkAir
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.subtract
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.formatBy

/**
 * 突破界面
 */
class BreakThroughUI(val player: Player) : ChestUI(
    PAPIHook.setPlaceHolderAndColor(BreakUIConfig.title, player),
    BreakUIConfig.row,
    BreakUIConfig.clickDelay
) {
    private var inputData: MMOForgeData? = null

    //    var limitSlot = mutableListOf<IOSlot>()
    private var canBreak = false
    private lateinit var inputSlot: IOSlot
    private lateinit var outputSlot: IOSlot
    private val breakThroughButtons = mutableListOf<Button>()
    private val materialSlots = mutableListOf<MaterialSlot>()
    private var gold = 0.0

    init {
        lockOnTop = false
        BreakUIConfig.slots["background"]?.forEach { (item, slots) ->
            val background = PAPIHook.setPlaceHolderAndColor(item, player)
            for (slot in slots) {
                Icon(background, slot).setup()
            }
        }
        BreakUIConfig.slots["materials"]?.forEach { (item, slots) ->
            for (slot in slots) {
                materialSlots.add(MaterialSlot(slot, PAPIHook.setPlaceHolderAndColor(item, player)).setup())
            }
        }
        BreakUIConfig.slots["input"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            inputSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item, player)).inputFilter {
                val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                inputData = nbtItem.getForgeData() ?: return@inputFilter false
                true
            }.onInput(async = true) {
                updateInput(inputData)
            }.onOutput(async = true) {
                inputData = null
                updateInput(null)
            }.setup()
        }
        BreakUIConfig.slots["output"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            outputSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item, player)).lockable(true).setup()
        }
        BreakUIConfig.slots["breakThrough"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            breakThroughButtons.add(
                Button(PAPIHook.setPlaceHolderAndColor(item, player), index)
                    .onClicked {
                        if (!canBreak) return@onClicked
                        if (!(it.whoClicked as Player).takeMoney(gold)) {
                            return@onClicked
                        }
                        //扣材料
                        for (materialSlot in materialSlots) {
                            val itemStack = materialSlot.itemStack ?: continue
                            itemStack.subtract()
                            if (!itemStack.checkAir()) {
                                materialSlot.ejectSilently(player)
                            }
                            materialSlot.reset()
                        }
                        outputSlot.outputAble(true)
                        //扣物品
                        inputSlot.reset()
                        reset()
                        canBreak = false
                        gold = 0.0
                        inputData = null
                    }.setup()
            )
        }
    }

    inner class MaterialSlot(slotIndex: Int, placeholder: ItemStack?) :
        IOSlot(slotIndex, placeholder) {
        var requireItem: String? = null

        init {
            //输入管理
            inputFilter {
                if (inputData == null || requireItem == null) false
                else {
                    val split = requireItem!!.split(':')
                    val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                    if (!nbtItem.hasType()) return@inputFilter false //目前仅支持mmo物品
                    val type = nbtItem.type
                    val id = nbtItem.getString("MMOITEMS_ITEM_ID")
                    type.equals(split[0], true) && id.equals(split[1], true)
                }
            }
            onInput(async = true) {
                updateResult()
            }
            onOutput(async = true) {
                updateResult()
            }
        }

        override fun reset() {
//            super.reset()
            requireItem = null
            updateDisplay()
        }

        /**
         * 更新占位符名称
         */
        fun updateDisplay() {
            if (requireItem == null) {
                placeholder = placeholder?.clone()?.applyMeta {
                    setDisplayName(
                        PAPIHook.setPlaceHolderAndColor(
                            BreakUIConfig.materialEmpty,
                            player
                        ),
                    )
                }
                itemStack = null
                return
            }
            val split = requireItem!!.split(":")
            if (split.size != 2) return
            val type = Type.get(split[0]) ?: return
            val template = MMOItems.plugin.templates.getTemplate(type, split[1]) ?: return
            placeholder = placeholder?.clone()?.applyMeta {
                setDisplayName(
                    PAPIHook.setPlaceHolderAndColor(
                        BreakUIConfig.materialRequired.formatBy((template.baseItemData[ItemStats.NAME] as NameData).bake()),
                        player
                    ),
                )
            }
            itemStack = null
        }
    }

    private fun resetResult() {
        breakThroughButtons.forEach { it.reset() }
        for (materialSlot in materialSlots) {
            materialSlot.ejectSilently(player)
            materialSlot.reset()
        }
        outputSlot.ejectSilently(player)
        outputSlot.reset()
        outputSlot.outputAble(false)
    }

    /**
     * 根据输入数据更新材料栏
     */
    private fun updateInput(inputData: MMOForgeData?) {
        if (inputData == null) {
            resetResult()
            return
        }
        val limitType = inputData.limitType[inputData.limit + 1]
        //刷新材料槽
        for ((i, s) in materialSlots.withIndex()) {
            s.requireItem = limitType?.getOrNull(i)
            s.updateDisplay()
        }
    }

    private fun updateResult() {
        if (inputData == null) {
            resetResult()
            return
        }
        //检测能否突破,可以则继续
        if (materialSlots.all { it.requireItem == null }) {
            resetResult()
            return
        }
        //检查材料是否完整
        if (materialSlots.any { it.requireItem != null && it.itemStack == null }) {
            breakThroughButtons.forEach { it.reset() }
            outputSlot.reset()
            return
        }
        val itemStack = inputSlot.itemStack ?: return
        val inputData = inputData!!.cloneData()
        val expression = MainConfig.goldForgeExpression.getString(inputData.star.toString()) ?: return
        //设置所需金币
        gold = MainConfig.getValueByFormula(expression, inputData.star, limit = 1)
        // 预览物品
        val liveMMOItem = LiveMMOItem(itemStack)
        liveMMOItem.breakthrough(inputData, 1)
        inputData.limit += 1
        liveMMOItem.setData(MMOForgeStat, inputData)
        //把旧的弹出
        outputSlot.ejectSilently(player)
        //上锁
        outputSlot.outputAble(false)
        outputSlot.itemStack = liveMMOItem.newBuilder().build()
        breakThroughButtons.forEach {
            it.itemStack = PAPIHook.setPlaceHolderAndColor(it.itemStack!!.applyMeta {
                setDisplayName(BreakUIConfig.breakThroughAllowed.formatBy(gold))
                addEnchant(Enchantment.BINDING_CURSE, 1, true)
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
            })
        }
        canBreak = true
    }
}