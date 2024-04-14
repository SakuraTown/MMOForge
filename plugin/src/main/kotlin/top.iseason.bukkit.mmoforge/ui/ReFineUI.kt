/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午9:18
 *
 */

package top.iseason.bukkit.mmoforge.ui

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.entity.Player
import top.iseason.bukkit.mmoforge.config.Lang
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.config.RefineUIConfig
import top.iseason.bukkit.mmoforge.hook.PAPIHook
import top.iseason.bukkit.mmoforge.hook.VaultHook.takeMoney
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkit.mmoforge.stats.RefineChance
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import top.iseason.bukkit.mmoforge.uitls.refine
import top.iseason.bukkit.mmoforge.uitls.setName
import top.iseason.bukkittemplate.ui.container.ChestUI
import top.iseason.bukkittemplate.ui.slot.*
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.getDisplayName
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.formatBy
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.other.EasyCoolDown
import top.iseason.bukkittemplate.utils.other.NumberUtils.toRoman
import top.iseason.bukkittemplate.utils.other.RandomUtils


class ReFineUI(val player: Player) : ChestUI(
    PAPIHook.setPlaceHolderAndColor(RefineUIConfig.title, player),
    RefineUIConfig.row,
    RefineUIConfig.clickDelay
) {
    private var toolMMOForgeData: MMOForgeData? = null
    private var materialMMOForgeData: MMOForgeData? = null
    private var toolType: String? = null
    private var gold = 0.0
    private var refine = 0
    private var newRefine = 0
    private var chance = 0.0
    private var materialChance = 0.0
    private lateinit var toolSlot: IOSlot
    private lateinit var materialSlot: IOSlot
    private lateinit var resultSlot: IOSlot
    private val refineButtons = mutableListOf<Button>()

    init {
        lockOnTop = false
        RefineUIConfig.slots["background"]?.forEach { (item, slots) ->
            val background = PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            for (slot in slots) {
                Icon(background, slot).setup()
            }
        }

        RefineUIConfig.slots["tool"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            toolSlot = IOSlot(
                index,
                PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            ).inputFilter {
                val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                toolMMOForgeData = nbtItem.getForgeData() ?: return@inputFilter false
                toolType = nbtItem.getString("MMOITEMS_ITEM_ID")
                true
            }.onOutput(true) {
                this@ReFineUI.reset()
            }.setup()
        }

        RefineUIConfig.slots["material"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            materialSlot = IOSlot(
                index, PAPIHook.setPlaceHolderAndColor(item.clone(), player)
            ).inputFilter {
                if (toolType == null || toolMMOForgeData == null) return@inputFilter false
                val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                if (nbtItem.getString("MMOITEMS_ITEM_ID") != toolType) return@inputFilter false
                materialMMOForgeData = nbtItem.getForgeData() ?: return@inputFilter false
                materialChance =
                    if (nbtItem.hasTag(RefineChance.stat.nbtPath)) nbtItem.getDouble(RefineChance.stat.nbtPath) else 0.0
                true
            }.onInput(true) {
                updateResult()
            }.onOutput(true) {
                resultSlot.reset()
                updateResult()
            }.setup()
        }

        RefineUIConfig.slots["result"]?.forEach { (item, slots) ->
            val index = slots.firstOrNull() ?: return@forEach
            resultSlot = IOSlot(index, item).inputAble(false).setup()
        }

        RefineUIConfig.slots["default-refine"]?.forEach { (item, slots) ->
            for (slot in slots) {
                refineButtons.add(
                    Button(
                        PAPIHook.setPlaceHolderAndColor(
                            item.clone(),
                            player
                        ), index = slot
                    ).onClicked(true) {
                        if (gold == 0.0) return@onClicked
                        val player = it.whoClicked as Player
                        if (!player.takeMoney(gold)) {
                            if (EasyCoolDown.check("${player.uniqueId}-ui_refine_no_gold", Lang.cooldown)) {
                                player.sendColorMessage(Lang.ui_refine_no_gold)
                            }
                            return@onClicked
                        }
                        if (chance < 100.0 && RandomUtils.checkPercentage(chance)) {
                            resultSlot.reset()
                            resultSlot.outputAble(false)
                            player.sendColorMessage(
                                Lang.ui_refine_failure.formatBy(
                                    refine,
                                    newRefine,
                                    resultSlot.itemStack?.getDisplayName()
                                )
                            )
                        } else {
                            resultSlot.outputAble(true)
                            player.sendColorMessage(
                                Lang.ui_refine_success.formatBy(
                                    refine,
                                    newRefine,
                                    resultSlot.itemStack?.getDisplayName()
                                )
                            )
                        }
                        resetData()
                        toolSlot.reset()
                        materialSlot.reset()
                        this.reset()
                    }.setup()
                )
            }
        }
    }

    private fun resetData() {
        toolMMOForgeData = null
        materialMMOForgeData = null
        toolType = null
        gold = 0.0
        refine = 0
        newRefine = 0
        chance = 0.0
        materialChance = 0.0
    }

    override fun reset() {
        resetData()
        ejectItems(player)
        super.reset()
    }

    private fun updateResult() {
        val toolM = toolSlot.itemStack
        val materialM = materialSlot.itemStack
        if (materialM == null) {
            refineButtons.forEach { it.reset() }
            gold = 0.0
            chance = 0.0
            refine = 0
            newRefine = 0
            materialChance = 0.0
            return
        }
        val toolNBT = NBTItem.get(toolM)
        if (!toolNBT.hasType()) {
            resultSlot.reset()
            refineButtons.forEach { it.reset() }
            gold = 0.0
            refine = 0
            chance = 0.0
            materialChance = 0.0
            newRefine = 0
            return
        }
        val mmoItem = LiveMMOItem(toolNBT)
        val forgeData = mmoItem.getData(MMOForgeStat) as? MMOForgeData ?: return
        var add = materialMMOForgeData!!.refine + 1
        add = if (forgeData.refine + add > forgeData.maxRefine) forgeData.maxRefine - forgeData.refine else add
        if (add == 0) return
        mmoItem.refine(forgeData, add)
        refine = forgeData.refine
        forgeData.refine += add
        newRefine = forgeData.refine
        mmoItem.setData(MMOForgeStat, forgeData)
        chance = if (mmoItem.hasData(RefineChance.stat)) {
            (mmoItem.getData(RefineChance.stat) as DoubleData).value
        } else {
            100.0
        } + materialChance
        val expression = MainConfig.goldForgeExpression.getString(forgeData.star.toString()) ?: return
        gold = MainConfig.getValueByFormula(expression, forgeData.star, refine = add)
        RefineUIConfig.slots["allow-refine"]?.forEach { (item, indexes) ->
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
        resultSlot.ejectSilently(player)
        resultSlot.outputAble(false)
        val buildSilently = mmoItem.newBuilder().build()
//        println(NBTItem.get(buildSilently).tags.contains("MMOITEMS_FIRE_DAMAGE"))
        buildSilently?.applyMeta {
            setName("$displayName ${forgeData.refine.toRoman()}")
        }
        resultSlot.itemStack = buildSilently
    }

}
