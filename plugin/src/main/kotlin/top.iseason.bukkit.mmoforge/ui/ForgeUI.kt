/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午7:26
 *
 */

package top.iseason.bukkit.mmoforge.ui

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import top.iseason.bukkit.mmoforge.config.ForgeUIConfig
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.hook.PAPIHook
import top.iseason.bukkit.mmoforge.hook.VaultHook.takeMoney
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.stats.material.ForgeExp
import top.iseason.bukkit.mmoforge.uitls.forge
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import top.iseason.bukkit.mmoforge.uitls.getMMODouble
import top.iseason.bukkittemplate.ui.container.ChestUI
import top.iseason.bukkittemplate.ui.slot.*
import top.iseason.bukkittemplate.utils.bukkit.EntityUtils.giveItems
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.subtract
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.formatBy


class ForgeUI(val player: Player) : ChestUI(
    PAPIHook.setPlaceHolderAndColor(ForgeUIConfig.title, player),
    ForgeUIConfig.row,
    ForgeUIConfig.clickDelay
) {
    private var inputData: MMOForgeData? = null
    private var canForge = false
    private var gold = 0.0
    private var costExp = 0.0
    private lateinit var inputSlot: IOSlot
    private lateinit var resultSlot: IOSlot
    private val materialSlots = mutableListOf<IOSlot>()
    private val forgeButtons = mutableListOf<Button>()

    init {
        //设置背景
        ForgeUIConfig.slots["background"]?.forEach { (item, slots) ->
            val background = PAPIHook.setPlaceHolderAndColor(item, player)
            for (slot in slots) {
                Icon(background, slot).setup()
            }
        }
        ForgeUIConfig.slots["input"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            inputSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item, player))
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
            val item2 = PAPIHook.setPlaceHolderAndColor(item, player)
            for (slot in slots) {
                materialSlots.add(
                    IOSlot(slot, item2).inputFilter {
                        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                        if (!nbtItem.hasType()) return@inputFilter false
                        val double = nbtItem.getDouble(ForgeExp.stat.nbtPath)
                        if (double == 0.0) return@inputFilter false
                        true
                    }.onInput(true) { updateResult() }
                        .onOutput(true) { updateResult() }
                        .setup()
                )
            }
        }
        ForgeUIConfig.slots["forge"]?.forEach { (item, slots) ->
            val item2 = PAPIHook.setPlaceHolderAndColor(item, player)
            for (slot in slots) {
                forgeButtons.add(
                    Button(item2, index = slot)
                        .onClicked(true) {
                            if (!canForge) return@onClicked
                            if (!(it.whoClicked as Player).takeMoney(gold)) {
                                return@onClicked
                            }
                            //扣材料
                            for (s in materialSlots) {
                                val material = s.itemStack ?: continue
                                val exp = NBTItem.get(material).getDouble(ForgeExp.stat.nbtPath)
                                if (exp == 0.0) continue
                                val times = (costExp / exp).toInt()
                                if (times <= material.amount) {
                                    material.subtract(times)
                                    if (material.type.isAir) s.itemStack = null
                                    break
                                }
                                s.itemStack = null
                                costExp -= exp * material.amount
                            }
                            inputSlot.reset()
                            reset()
                            resultSlot.outputAble(true)
                            canForge = false
                            gold = 0.0
                            costExp = 0.0
                        }.setup()
                )
            }
        }
        ForgeUIConfig.slots["result"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            resultSlot = IOSlot(index, PAPIHook.setPlaceHolderAndColor(item, player)).lockable(true).setup()
        }
    }

    /**
     * 更新界面
     */
    private fun updateResult() {
        var totalExp = 0.0
        materialSlots.forEach {
            val itemStack = it.itemStack ?: return@forEach
            totalExp += itemStack.getMMODouble(ForgeExp.stat.nbtPath) * itemStack.amount
        }
        val inputItem = inputSlot.itemStack
        //不满足强化条件
        if (inputItem == null || totalExp == 0.0 || inputData == null) {
            resetResult()
            return
        }
        val forgeData = inputData!!.cloneData()
        val (level, remain, overflow) = forgeData.getLevelByExtraExp(totalExp)
        //不能升级
        if (overflow == totalExp) {
            resetResult()
            return
        }
        costExp = totalExp - overflow
        val expression = MainConfig.goldForgeExpression.getString(forgeData.star.toString()) ?: return
        gold = MainConfig.getValueByFormula(expression, forgeData.star, forge = costExp)
        val liveMMOItem = LiveMMOItem(inputItem)
        liveMMOItem.forge(forgeData, level)
        forgeData.apply {
            forge += level
            currentExp = remain
        }
        liveMMOItem.setData(MMOForgeStat, forgeData)
        resultSlot.ejectSilently(player)
        resultSlot.outputAble(false)
        resultSlot.itemStack = liveMMOItem.newBuilder().buildSilently()

        forgeButtons.forEach {
            it.itemStack = PAPIHook.setPlaceHolderAndColor(it.itemStack!!.applyMeta {
                setDisplayName(ForgeUIConfig.forgeAllowed.formatBy(gold))
                addEnchant(Enchantment.BINDING_CURSE, 1, true)
                addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }, player)
        }
        canForge = true
    }

    private fun resetResult() {
        forgeButtons.forEach { it.reset() }
        canForge = false
        gold = 0.0
        costExp = 0.0
        val itemStack = resultSlot.itemStack
        if (itemStack != null && resultSlot.output(resultSlot, itemStack))
            getViewers().getOrNull(0)?.giveItems(itemStack)
        resultSlot.reset()
    }
}