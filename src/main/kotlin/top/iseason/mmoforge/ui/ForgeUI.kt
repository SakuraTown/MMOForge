/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午7:26
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
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.stats.MMOForgeStat
import top.iseason.mmoforge.stats.material.ForgeExp
import top.iseason.mmoforge.uitls.*


class ForgeUI : ChestUI("物品强化", 6) {
    private var inputData: MMOForgeData? = null

    private var canForge = false
    private var gold = 0.0
    private var costExp = 0.0

    //强化材料槽
    private val materials = IOSlot(29, ItemStack(Material.YELLOW_STAINED_GLASS_PANE).applyMeta {
        setName("&6 请放入强化材料")
    }).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        if (!nbtItem.hasType()) return@inputFilter false
        val double = nbtItem.getDouble(ForgeExp.stat.nbtPath)
        if (double == 0.0) return@inputFilter false
        true
    }.onInput { updateAsync() }.onOutput { updateAsync() }

    init {
        //设置背景
        setBackGround(Icon(ItemStack(Material.GRAY_STAINED_GLASS_PANE).applyMeta {
            setName(" ")
        }, 1))
    }

    private val materialSlots: List<IOSlot> = addMultiSlots(materials, 30, 31, 32, 33).toType()

    //输入槽
    private val inputSlot = IOSlot(13, ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
        setName("${ChatColor.RED} 请放入待强化的物品")
    }).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        inputData = nbtItem.getForgeData() ?: return@inputFilter false
        true
    }.onInput {
        updateAsync()
    }.onOutput {
        inputData = null
        updateAsync()
    }.setUI(this)

    private val resultSlot = IOSlot(49).lockable(true).setUI(this)

    private val forgeButton =
        Button(ItemStack(Material.ANVIL).applyMeta { setName("${ChatColor.RED}无法强化") }, index = 40)
            .onClicked {
                if (!canForge) return@onClicked
                if (!(it.whoClicked as Player).takeMoney(gold)) {
                    return@onClicked
                }
                //扣材料
                for (slot in materialSlots) {
                    val material = slot.itemStack ?: continue
                    val exp = NBTItem.get(material).getDouble(ForgeExp.stat.nbtPath)
                    if (exp == 0.0) continue
                    val times = (costExp / exp).toInt()
                    if (times <= material.amount) {
                        material.subtract(times)
                        break
                    }
                    slot.itemStack = null
                    costExp -= exp * material.amount
                }
                inputSlot.reset()
                reset()
                resultSlot.outputAble(true)
                canForge = false
                gold = 0.0
                costExp = 0.0
            }.setUI(this)

    private fun updateAsync() {
        submit(async = true) {
            updateResult()
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

        submit {
            resultSlot.outputAble(false)
            resultSlot.itemStack = liveMMOItem.newBuilder().build()
        }

        forgeButton.displayName = "${ChatColor.GREEN}点击强化物品: ${ChatColor.GOLD}$gold ￥"
        forgeButton.itemStack!!.applyMeta {
            addEnchant(Enchantment.BINDING_CURSE, 1, true)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        canForge = true
    }

    private fun resetResult() {
        forgeButton.reset()
        canForge = false
        gold = 0.0
        costExp = 0.0
        val itemStack = resultSlot.itemStack
        if (itemStack != null && resultSlot.output(resultSlot, itemStack))
            getViewers().getOrNull(0)?.giveItems(itemStack)
        resultSlot.reset()
    }
}