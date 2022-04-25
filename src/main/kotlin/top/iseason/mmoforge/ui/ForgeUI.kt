/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午7:26
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.ui.*
import io.lumine.mythic.lib.api.item.NBTItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.uitls.getForgeData


class ForgeUI : ChestUI("物品强化") {
    private var inputData: MMOForgeData? = null

    //强化材料槽
    private val materials = IOSlot(29).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        if (!nbtItem.hasType()) return@inputFilter false
        val double = nbtItem.getDouble("MMOITEMS_FORGE_EXP")
        if (double == 0.0) return@inputFilter false
        true
    }

    init {
        //设置背景
        setBackGround(Icon(ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply { setDisplayName(" ") }
        }, 1))
        addMultiSlots(materials, 30, 31, 32, 33)
    }

    //输入槽
    private val input = IOSlot(13).inputFilter {
        val nbtItem = NBTItem.get(it) ?: return@inputFilter false
        inputData = nbtItem.getForgeData() ?: return@inputFilter false
        true
    }.setUI(this).onInput {
    }.onOutput {

    }

    private val output = IOSlot(49).lockable(true).setUI(this)

    fun getMaterials() {
        for (i in 29..33) {
            getSlot(i)
        }
    }
}