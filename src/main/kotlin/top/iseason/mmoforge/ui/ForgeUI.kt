/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/21 下午7:26
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.ui.ChestUI
import org.bukkit.Material

class ForgeUI : ChestUI("物品强化") {
    init {//设置占位符
        setMultiSlots(Icon(Material.GRAY_STAINED_GLASS_PANE, " "), (1..53).toList())
    }
    //todo:完善强化界面

}