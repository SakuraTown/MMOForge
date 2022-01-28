/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/27 下午9:20
 *
 */

package top.iseason.rpgforgesystem

import com.entiv.core.menu.Button
import com.entiv.core.menu.Menu

class ForgeMenu : Menu(RPGForgeSystem.instance, "强化", 6, lock = true) {
    init {
        setButton(Button(slots = intArrayOf(0), onClick = {}, lock = true))
    }
}