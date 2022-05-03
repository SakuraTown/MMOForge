/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/26 下午10:20
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.common.submit
import com.entiv.core.ui.*
import com.entiv.core.utils.bukkit.applyMeta
import com.entiv.core.utils.bukkit.giveItems
import com.entiv.core.utils.bukkit.takeMoney
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.Type
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.type.NameData
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.stats.MMOForgeStat
import top.iseason.mmoforge.uitls.breakthrough
import top.iseason.mmoforge.uitls.getForgeData
import top.iseason.mmoforge.uitls.setName
import top.iseason.mmoforge.uitls.toType

//todo: 完成突破界面 -> 材料输入产出结果
class BreakThroughUI : ChestUI("物品突破", 6) {
    private var inputData: MMOForgeData? = null
    var limitSlot = mutableListOf<IOSlot>()
    private var canBreak = false
    private var gold = 0.0
    private val rawPlaceHolder = ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
        setName("${ChatColor.RED}请先放入需要突破的物品")
    }

    //强化材料槽
    private val materials = IOSlot(30, rawPlaceHolder).inputAble(false).onInput {
        updateResultAsync()
    }.onOutput {
        updateResultAsync()
    }

    init {
        //设置背景
        setBackGround(Icon(ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply { setName(" ") }
        }, 1))
    }

    private val materialSlots: List<IOSlot> = addMultiSlots(materials, 31, 32).toType()

    //输入槽
    private val inputSlot = IOSlot(13, ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
        setName("${ChatColor.RED} 请放入待突破的物品")
    }).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        inputData = nbtItem.getForgeData() ?: return@inputFilter false
        true
    }.onInput {
        updateInputAsync()
    }.onOutput {
        inputData = null
        updateInputAsync()
    }.setUI(this)

    private val resultSlot = IOSlot(49).lockable(true).setUI(this)

    private val breakThroughButton =
        Button(ItemStack(Material.ANVIL).applyMeta { setName("${ChatColor.RED}无法突破") }, index = 40)
            .onClicked {
                if (!canBreak) return@onClicked
                if (!(it.whoClicked as Player).takeMoney(gold)) {
                    return@onClicked
                }
                //扣材料
                for (ioSlot in limitSlot) {
                    ioSlot.itemStack?.subtract()
                }
                for (materialSlot in materialSlots) {
                    val itemStack = materialSlot.itemStack
                    if (itemStack != null && materialSlot.output(materialSlot, itemStack))
                        getViewers().getOrNull(0)?.giveItems(itemStack)
                    materialSlot.placeholder = rawPlaceHolder
                    materialSlot.itemStack = null
                    materialSlot.inputAble(false)
                }
                resultSlot.outputAble(true)
                //扣物品
                inputSlot.reset()
                reset()
                limitSlot.clear()
                canBreak = false
                gold = 0.0
                inputData = null
            }.setUI(this)

    private fun updateInputAsync() {
        submit(async = true) {
            updateInput(inputData)
        }
    }

    private fun resetResult() {
        breakThroughButton.reset()
        limitSlot.clear()
        for (materialSlot in materialSlots) {
            val itemStack = materialSlot.itemStack
            if (itemStack != null && materialSlot.output(materialSlot, itemStack))
                getViewers().getOrNull(0)?.giveItems(itemStack)
            materialSlot.placeholder = rawPlaceHolder
            materialSlot.itemStack = null
            materialSlot.inputAble(false)
        }
        val itemStack = resultSlot.itemStack
        if (itemStack != null && resultSlot.output(resultSlot, itemStack))
            getViewers().getOrNull(0)?.giveItems(itemStack)
        resultSlot.reset()
        resultSlot.outputAble(false)
    }

    /**
     * 根据输入数据更新材料栏
     */
    private fun updateInput(inputData: MMOForgeData?) {
        if (inputData == null) {
            resetResult()
            return
        }
        //处理输入物品
        val limitType = inputData.limitType[inputData.limit + 1]
        //显示需要的突破材料

        limitType?.forEachIndexed { index, str ->
            val split = str.split(":")
            if (split.size != 2) return@forEachIndexed
            val type = Type.get(split[0]) ?: return@forEachIndexed
            val template = MMOItems.plugin.templates.getTemplate(type, split[1]) ?: return@forEachIndexed
            val name =
                "${ChatColor.YELLOW}请放入:${ChatColor.RESET} ${(template.baseItemData[ItemStats.NAME] as NameData).bake()}"
            val ioSlot = materialSlots.getOrNull(index) ?: return@forEachIndexed
            ioSlot.placeholder = ItemStack(Material.YELLOW_STAINED_GLASS_PANE).clone().applyMeta {
                setName(name)
            }
            ioSlot.itemStack = null
            limitSlot.add(ioSlot)
            ioSlot.inputFilter {
                val nbtItem = NBTItem.get(it) ?: return@inputFilter false
                if (!nbtItem.hasType()) return@inputFilter false
                val type1 = nbtItem.type
                val id = nbtItem.getString("MMOITEMS_ITEM_ID")
                type1.equals(split[0], true) && id.equals(split[1], true)
            }
        }
        if (limitSlot.isEmpty()) {
            for (materialSlot in materialSlots) {
                materialSlot.placeholder = ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
                    setName("${ChatColor.RED}该物品无法突破")
                }
                materialSlot.itemStack = null
            }
        }
    }

    private fun updateResultAsync() {
        submit(async = true) {
            updateResult()
        }
    }

    private fun updateResult() {
        if (inputData == null) {
            resetResult()
            return
        }
        //检测能否突破
        if (limitSlot.isEmpty()) {
            resetResult()
            return
        }
        //检查材料是否完整和突破次数
        for (ioSlot in limitSlot) {
            val itemStack = ioSlot.itemStack
            if (itemStack == null) {
                breakThroughButton.reset()
                resultSlot.reset()
                return
            }
        }
        val itemStack = inputSlot.itemStack ?: return
        val inputData = inputData!!.cloneData()
        val expression = MainConfig.goldForgeExpression.getString(inputData.star.toString()) ?: return
        gold = MainConfig.getValueByFormula(expression, inputData.star, limit = 1)
        val liveMMOItem = LiveMMOItem(itemStack)
        liveMMOItem.breakthrough(inputData, 1)
        inputData.limit += 1
        liveMMOItem.setData(MMOForgeStat, inputData)
        submit {
            resultSlot.outputAble(false)
            resultSlot.itemStack = liveMMOItem.newBuilder().build()
        }
        breakThroughButton.displayName = "${ChatColor.GREEN}点击突破物品: ${ChatColor.GOLD}$gold ￥"
        breakThroughButton.itemStack!!.applyMeta {
            addEnchant(Enchantment.BINDING_CURSE, 1, true)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        canBreak = true
    }
}