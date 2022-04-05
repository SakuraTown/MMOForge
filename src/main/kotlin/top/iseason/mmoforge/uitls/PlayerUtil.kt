
/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/24 下午11:35
 *
 */

package top.iseason.mmoforge.uitls

import com.entiv.core.hook.VaultEconomyHook
import com.entiv.core.utils.sendErrorMessage
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.entity.Player

fun Player.takeMoney(money: Double): Boolean {
    if (VaultEconomyHook.has(this, money) == true) {
        val response = VaultEconomyHook.withdraw(this, money)
        if (response?.type != EconomyResponse.ResponseType.SUCCESS) {
            sendErrorMessage(this, "余额不足!")
            return false
        }
    } else {
        sendErrorMessage(this, "余额不足!")
        return false
    }
    return true
}


