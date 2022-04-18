/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/18 下午9:18
 *
 */

package top.iseason.mmoforge.ui

import com.entiv.core.ui.ChestUI
import org.bukkit.Material

// todo: 完成物品精炼
class ReFineUI : ChestUI("物品精炼") {
    init {//设置占位符
        setMultiSlots(Icon(Material.GRAY_STAINED_GLASS_PANE, " "), (1..53).toList())
    }
}