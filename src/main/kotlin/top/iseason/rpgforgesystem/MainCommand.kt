/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/19 下午9:48
 *
 */

package top.iseason.rpgforgesystem


import com.entiv.core.command.SimpleCommand
import top.iseason.rpgforgesystem.uitls.modifyRefine

class MainCommand : SimpleCommand(RPGForgeSystem.instance, true, name = "test", isPlayerOnly = true) {
    override fun onCommand() {
        val itemInMainHand = player?.inventory?.itemInMainHand ?: return
//        val liveMMOItem = LiveMMOItem(itemInMainHand)
//        liveMMOItem.getData(ItemStats.NAME).sendConsole()
//        liveMMOItem.getData(ItemStats.ATTACK_DAMAGE).sendConsole()
        player?.inventory?.setItemInMainHand(itemInMainHand.modifyRefine(1))
//        player!!.send(player?.inventory?.itemInMainHand?.getRPGData(Config.QUALITY_TAG))
    }
}