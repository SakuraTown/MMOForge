/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/9/30 下午7:30
 *
 */

package top.iseason.bukkit.mmoforge.hook

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit.getServer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import top.iseason.bukkittemplate.hook.BaseHook

object VaultHook : BaseHook("Vault") {
    private val economy = getServer().servicesManager.getRegistration(Economy::class.java)?.provider

    fun Player.takeMoney(amount: Double): Boolean {
        if (economy == null) return false
        return if (has(this, amount) == true) {
            withdraw(this, amount)?.type == EconomyResponse.ResponseType.SUCCESS
        } else
            false
    }

    fun withdraw(player: OfflinePlayer, amount: Double): EconomyResponse? =
        economy?.withdrawPlayer(player, amount)

    fun deposit(player: OfflinePlayer, amount: Double) = economy?.depositPlayer(player, amount)
    fun has(player: OfflinePlayer, amount: Double) = economy?.has(player, amount)
}