/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午7:26
 *
 */

package top.iseason.bukkit.mmoforge.ui

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.entity.Player
import top.iseason.bukkit.mmoforge.config.ForgeUIConfig
import top.iseason.bukkit.mmoforge.config.Lang
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.hook.PAPIHook
import top.iseason.bukkit.mmoforge.hook.VaultHook.takeMoney
import top.iseason.bukkit.mmoforge.stats.ForgeChance
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.stats.material.ForgeExp
import top.iseason.bukkit.mmoforge.uitls.forge
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import top.iseason.bukkit.mmoforge.uitls.hasForgeData
import top.iseason.bukkittemplate.ui.container.ChestUI
import top.iseason.bukkittemplate.ui.slot.*
import top.iseason.bukkittemplate.utils.bukkit.EntityUtils.giveItems
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.decrease
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.getDisplayName
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.formatBy
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.other.EasyCoolDown
import top.iseason.bukkittemplate.utils.other.RandomUtils
import top.iseason.bukkittemplate.utils.other.submit
import kotlin.math.ceil


class ForgeUI(val player: Player) : ChestUI(
    PAPIHook.setPlaceHolderAndColor(ForgeUIConfig.title, player),
    ForgeUIConfig.row,
    ForgeUIConfig.clickDelay
) {
    private var inputData: MMOForgeData? = null
    private var canForge = false
    private var forgeLevel = 0
    private var newForgeLevel = 0
    private var gold = 0.0
    private var costExp = 0.0
    private var chance = 0.0
    private lateinit var inputSlot: IOSlot
    private lateinit var resultSlot: IOSlot
    private val materialSlots = mutableListOf<IOSlot>()
    private val forgeButtons = mutableListOf<Button>()

    init {
        lockOnTop = false
        //设置背景
        ForgeUIConfig.slots["background"]?.forEach { (item, slots) ->
            val background = PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            for (slot in slots) {
                Icon(background, slot).setup()
            }
        }
        ForgeUIConfig.slots["input"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            inputSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item.clone(), player))
                .inputFilter {
                    val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                    inputData = nbtItem.getForgeData() ?: return@inputFilter false
                    true
                }.onInput(true) {
                    updateResult()
                }.onOutput(true) {
                    inputData = null
                    updateResult()
                }.setup()
        }
        ForgeUIConfig.slots["material"]?.forEach { (item, slots) ->
            val item2 = PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            for (slot in slots) {
                materialSlots.add(
                    IOSlot(slot, item2).inputFilter {
                        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                        if (!nbtItem.hasType()) return@inputFilter false
                        val double = nbtItem.getDouble(ForgeExp.stat.nbtPath)
                        if (double == 0.0) {
                            val chance = nbtItem.getDouble(ForgeChance.stat.nbtPath)
                            if (chance != 0.0 && !nbtItem.hasForgeData()) {
                                for (materialSlot in materialSlots) {
                                    val sItem = materialSlot.itemStack ?: continue
                                    if (NBTItem.get(sItem).hasTag(ForgeChance.stat.nbtPath)) {
                                        return@inputFilter false
                                    }
                                }
                                return@inputFilter true
                            }
                            return@inputFilter false
                        }
                        true
                    }.onInput(true) { updateResult() }
                        .onOutput(true) {
                            updateResult()
                        }
                        .setup()
                )
            }
        }
        ForgeUIConfig.slots["default-forge"]?.forEach { (item, slots) ->
            val item2 = PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            for (slot in slots) {
                forgeButtons.add(
                    Button(item2, index = slot)
                        .onClicked {
                            if (!canForge) return@onClicked
                            val player = it.whoClicked as Player
                            if (!player.takeMoney(gold)) {
                                if (EasyCoolDown.check("${player.uniqueId}-ui_forge_no_gold", Lang.cooldown)) {
                                    player.sendColorMessage(Lang.ui_forge_no_gold)
                                }
                                return@onClicked
                            }
                            //扣材料
                            for (s in materialSlots) {
                                val material = s.itemStack ?: continue
                                val get = NBTItem.get(material)
                                val amount = material.amount
                                val exp = get.getDouble(ForgeExp.stat.nbtPath)
                                if (exp == 0.0) { //强化概率只扣一个
                                    val chance = get.getDouble(ForgeChance.stat.nbtPath)
                                    if (chance > 0) {
                                        if (amount > 1)
                                            material.decrease(1)
                                        else
                                            s.reset()
                                    }
                                } else {
                                    val times = ceil(costExp / exp).toInt()
                                    if (times < amount) {
                                        costExp -= exp * times
                                        material.decrease(times)
                                    } else {
                                        costExp -= exp * amount
                                        s.reset()
                                    }
                                }
                            }

                            if (chance < 100.0 && RandomUtils.checkPercentage(chance)) {
                                player.sendColorMessage(
                                    Lang.ui_forge_failure.formatBy(
                                        forgeLevel,
                                        newForgeLevel,
                                        resultSlot.itemStack?.getDisplayName()
                                    )
                                )
                                if (MainConfig.forgeFailureRemoveItem) {
                                    inputSlot.reset()
                                } else {
                                    submit {
                                        val tItem = inputSlot.itemStack ?: return@submit
                                        inputData = NBTItem.get(tItem).getForgeData()
                                        inputSlot.onInput.invoke(inputSlot, tItem)
                                    }
                                }
                                resultSlot.reset()
                                resultSlot.outputAble(false)

                            } else {
                                inputSlot.reset()
                                resultSlot.outputAble(true)
                                player.sendColorMessage(
                                    Lang.ui_forge_success.formatBy(
                                        forgeLevel,
                                        newForgeLevel,
                                        resultSlot.itemStack?.getDisplayName()
                                    )
                                )
                            }
                            reset()
                            canForge = false
                            gold = 0.0
                            costExp = 0.0
                            forgeLevel = 0
                            newForgeLevel = 0
                            chance = 0.0
                        }.setup()
                )
            }
        }
        ForgeUIConfig.slots["result"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            resultSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item.clone(), player)).lockable(true).setup()
        }
    }

    /**
     * 更新界面
     */
    private fun updateResult() {
        var totalExp = 0.0
        var materialChance = 0.0
        materialSlots.forEach {
            val itemStack = it.itemStack ?: return@forEach
            val get = NBTItem.get(itemStack)
            val exp = get.getDouble(ForgeExp.stat.nbtPath)
            totalExp += exp * itemStack.amount
            materialChance += get.getDouble(ForgeChance.stat.nbtPath)
        }

        val inputItem = inputSlot.itemStack
        //不满足强化条件
        if (inputItem == null || totalExp == 0.0 || inputData == null) {
            resetResult()
            return
        }
        val forgeData = inputData!!.clone()
        val (level, remain, overflow) = forgeData.getLevelByExtraExp(totalExp)
        //不能升级
        if (overflow == totalExp) {
            resetResult()
            ForgeUIConfig.slots["max-forge"]?.forEach { (item, indexes) ->
                val stack = PAPIHook.setPlaceHolderAndColor(item.clone(), player).applyMeta {
                    if (hasDisplayName()) setDisplayName(
                        displayName.replace("{gold}", gold.toString())
                            .replace("{chance}", chance.toString())
                    )
                    if (hasLore()) lore = lore!!.map {
                        it.replace("{chance}", chance.toString())
                            .replace("{gold}", gold.toString())
                    }
                }
                for (index in indexes) {
                    getSlot(index)?.itemStack = stack
                }
            }
            return
        }
        costExp = totalExp - overflow
        val expression = MainConfig.goldForgeExpression.getString(forgeData.star.toString()) ?: return
        gold = MainConfig.getValueByFormula(
            expression,
            forgeData.star,
            forge = level,
            exp = costExp,
            nowForge = forgeData.forge,
            nowLimit = forgeData.limit,
            nowRefine = forgeData.refine,
        )
        val liveMMOItem = LiveMMOItem(inputItem)
        chance = if (liveMMOItem.hasData(ForgeChance.stat)) {
            (liveMMOItem.getData(ForgeChance.stat) as DoubleData).value
        } else {
            100.0
        } + materialChance
        liveMMOItem.forge(forgeData, level)
        forgeLevel = forgeData.forge
        forgeData.apply {
            forge += level
            currentExp = remain
        }
        newForgeLevel = forgeData.forge
        liveMMOItem.setData(MMOForgeStat, forgeData)
        resultSlot.ejectSilently(player)
        resultSlot.outputAble(false)
        resultSlot.itemStack = liveMMOItem.newBuilder().build()
        ForgeUIConfig.slots["allow-forge"]?.forEach { (item, indexes) ->
            val stack = PAPIHook.setPlaceHolderAndColor(item.clone(), player).applyMeta {
                if (hasDisplayName()) setDisplayName(
                    displayName.replace("{gold}", gold.toString())
                        .replace("{chance}", chance.toString())
                )
                if (hasLore()) lore = lore!!.map {
                    it.replace("{chance}", chance.toString())
                        .replace("{gold}", gold.toString())
                }
            }
            for (index in indexes) {
                getSlot(index)?.itemStack = stack
            }
        }
        canForge = true
    }

    private fun resetResult() {
        forgeButtons.forEach { it.reset() }
        canForge = false
        gold = 0.0
        costExp = 0.0
        forgeLevel = 0
        newForgeLevel = 0
        chance = 0.0
        val itemStack = resultSlot.itemStack
        if (itemStack != null && resultSlot.output(resultSlot, itemStack))
            getViewers().getOrNull(0)?.giveItems(itemStack)
        resultSlot.reset()
    }
}