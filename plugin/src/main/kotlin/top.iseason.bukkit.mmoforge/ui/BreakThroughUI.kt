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
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.NameData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import top.iseason.bukkit.mmoforge.config.BreakUIConfig
import top.iseason.bukkit.mmoforge.config.Lang
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.hook.PAPIHook
import top.iseason.bukkit.mmoforge.hook.VaultHook.takeMoney
import top.iseason.bukkit.mmoforge.stats.BreakChance
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.uitls.breakthrough
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import top.iseason.bukkittemplate.ui.container.ChestUI
import top.iseason.bukkittemplate.ui.slot.*
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.checkAir
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.decrease
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.getDisplayName
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.formatBy
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.bukkit.SchedulerUtils.submit
import top.iseason.bukkittemplate.utils.other.EasyCoolDown
import top.iseason.bukkittemplate.utils.other.RandomUtils

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
    private var breakLevel = 0
    private var newBreakLevel = 0
    private var chance = 0.0

    init {
        lockOnTop = false
        BreakUIConfig.slots["background"]?.forEach { (item, slots) ->
            val background = PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            for (slot in slots) {
                Icon(background, slot).setup()
            }
        }
        BreakUIConfig.slots["default-materials"]?.forEach { (item, slots) ->
            for (slot in slots) {
                materialSlots.add(MaterialSlot(slot, PAPIHook.setPlaceHolderAndColor(item.clone(), player)).setup())
            }
        }
        BreakUIConfig.slots["input"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            inputSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item.clone(), player))
                .inputFilter {
                    val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                    val inputData = nbtItem.getForgeData() ?: return@inputFilter false
                    if (inputData.forge != inputData.getCurrentMaxForge()) return@inputFilter false
                    this@BreakThroughUI.inputData = inputData
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
            outputSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item.clone(), player)).lockable(true).setup()
        }
        BreakUIConfig.slots["default-break"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            breakThroughButtons.add(
                Button(PAPIHook.setPlaceHolderAndColor(item.clone(), player), index)
                    .onClicked {
                        if (!canBreak) return@onClicked
                        val player = it.whoClicked as Player
                        if (!player.takeMoney(gold)) {
                            if (EasyCoolDown.check("${player.uniqueId}-ui_break_no_gold", Lang.cooldown)) {
                                player.sendColorMessage(Lang.ui_break_no_gold)
                            }
                            return@onClicked
                        }
                        //扣材料
                        for (materialSlot in materialSlots) {
                            val itemStack = materialSlot.itemStack ?: continue
                            itemStack.decrease()
                            if (!itemStack.checkAir()) {
                                materialSlot.ejectSilently(player)
                            }
                            materialSlot.reset()
                        }
                        //扣物品
                        reset()
                        if (chance < 100.0 && RandomUtils.checkPercentage(chance)) {
                            if (MainConfig.breakFailureRemoveItem) {
                                inputSlot.reset()
                            } else {
                                submit {
                                    val tItem = inputSlot.itemStack ?: return@submit
                                    inputData = NBTItem.get(tItem).getForgeData()
                                    inputSlot.onInput.invoke(inputSlot, tItem)
                                }
                            }
                            outputSlot.reset()
                            outputSlot.outputAble(false)
                            player.sendColorMessage(
                                Lang.ui_break_failure.formatBy(
                                    breakLevel,
                                    newBreakLevel,
                                    outputSlot.itemStack?.getDisplayName()
                                )
                            )
                        } else {
                            inputSlot.reset()
                            outputSlot.outputAble(true)
                            player.sendColorMessage(
                                Lang.ui_break_success.formatBy(
                                    breakLevel,
                                    newBreakLevel,
                                    outputSlot.itemStack?.getDisplayName()
                                )
                            )
                        }
                        canBreak = false
                        gold = 0.0
                        chance = 0.0
                        inputData = null
                        breakLevel = 0
                        newBreakLevel = 0
                    }.setup()
            )
        }
    }

    inner class MaterialSlot(slotIndex: Int, placeholder: ItemStack?) :
        IOSlot(slotIndex, placeholder) {

        private val basePlaceholder = placeholder
        var requireItem: String? = null

        var chance = 0.0

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
                    val result = type.equals(split[0], true) && id.equals(split[1], true)
                    result
                }
            }
            onInput(async = true) {
                val nbtItem = NBTItem.get(it)
                if (nbtItem.hasTag(BreakChance.stat.nbtPath)) {
                    chance = nbtItem.getDouble(BreakChance.stat.nbtPath)
                }
                updateResult()
            }
            onOutput(async = true) {
                chance = 0.0
                updateResult()
            }
        }

        override fun reset() {
//            super.reset()
            requireItem = null
            chance = 0.0
            updateDisplay()
        }

        /**
         * 更新占位符名称
         */
        fun updateDisplay() {
            if (requireItem == null) {
                placeholder = basePlaceholder
                itemStack = null
                return
            }
            val split = requireItem!!.split(":")
            if (split.size != 2) return
            val type = Type.get(split[0]) ?: return
            val template = MMOItems.plugin.templates.getTemplate(type, split[1]) ?: return
            val item = BreakUIConfig.slots["allow-materials"]?.entries?.firstNotNullOfOrNull {
                it.value.contains(this@MaterialSlot.index)
                it.key
            }
            if (item != null) {
                val typeName = type.name
                val itemName = (template.baseItemData[ItemStats.NAME] as NameData).bake()
                placeholder = item.clone().applyMeta {
                    if (hasDisplayName())
                        setDisplayName(
                            PAPIHook.setPlaceHolderAndColor(displayName.formatBy(typeName, itemName), player),
                        )
                    if (hasLore()) lore =
                        lore!!.map { PAPIHook.setPlaceHolderAndColor(it.formatBy(typeName, itemName), player) }
                }
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
        if (inputData!!.limit >= inputData!!.maxLimit) {
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
        val inputData = inputData!!.clone()
        val expression = MainConfig.goldBreakExpression.getString(inputData.star.toString()) ?: return
        //设置所需金币
        gold = MainConfig.getValueByFormula(
            expression,
            inputData.star,
            limit = 1,
            nowForge = inputData.forge,
            nowLimit = inputData.limit,
            nowRefine = inputData.refine,
        )
        // 预览物品
        val liveMMOItem = LiveMMOItem(itemStack)
        liveMMOItem.breakthrough(inputData, 1)
        breakLevel = inputData.limit
        inputData.limit += 1
        newBreakLevel = inputData.limit
        liveMMOItem.setData(MMOForgeStat, inputData)
        chance = if (liveMMOItem.hasData(BreakChance.stat)) {
            (liveMMOItem.getData(BreakChance.stat) as DoubleData).value
        } else {
            100.0
        } + materialSlots.sumOf { it.chance }
        //把旧的弹出
        outputSlot.ejectSilently(player)
        //上锁
        outputSlot.outputAble(false)
        val oldName = itemStack.getDisplayName()
        outputSlot.itemStack = liveMMOItem.newBuilder().build()?.applyMeta {
            setDisplayName(oldName)
        }
        val modelDataHis = liveMMOItem.getStatHistory(ItemStats.CUSTOM_MODEL_DATA)
        if (modelDataHis != null) {
            val modelData = modelDataHis.recalculate(liveMMOItem.upgradeLevel) as DoubleData
            outputSlot.itemStack!!.applyMeta { this.setCustomModelData(modelData.value.toInt()) }
        }
        BreakUIConfig.slots["allow-break"]?.forEach { (item, indexes) ->
            val stack = PAPIHook.setPlaceHolderAndColor(item.clone(), player).applyMeta {
                if (hasDisplayName()) setDisplayName(
                    displayName.replace("{gold}", gold.toString())
                        .replace("{chance}", chance.toString())
                )
                if (hasLore()) lore = lore!!.map {
                    it
                        .replace("{gold}", gold.toString())
                        .replace("{chance}", chance.toString())
                }
            }
            for (index in indexes) {
                getSlot(index)?.itemStack = stack
            }
        }
        canBreak = true
    }
}