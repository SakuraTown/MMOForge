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
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack

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

/**
 * 在方块处掉落该方块相应的掉落物(使用指定工具)
 */
fun Player.dropBlock(block: Block, tool: ItemStack?) {
    val drops = block.getDrops(tool)
    val itemList = mutableListOf<Item>()
    for (drop in drops) {
        itemList.add(dropItemNaturally(block.location, drop))
    }
    val blockDropItemEvent = BlockDropItemEvent(block, block.state, this, itemList)
    Bukkit.getServer().pluginManager.callEvent(blockDropItemEvent)
    if (blockDropItemEvent.isCancelled) {
        itemList.forEach { it.remove() }
    }

}


