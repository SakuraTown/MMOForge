/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/10/1 下午7:44
 *
 */

package top.iseason.bukkit.mmoforge.config

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import top.iseason.bukkit.mmoforge.config.BreakUIConfig.readSlot
import top.iseason.bukkit.mmoforge.config.BreakUIConfig.readSlots
import top.iseason.bukkit.mmoforge.uitls.setName
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.getItem
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.toSection

@FilePath("ui/forge.yml")
object ForgeUIConfig : SimpleYAMLConfig() {

    @Key
    @Comment("ui标题")
    var title = "物品强化"

    @Key
    @Comment("ui行数")
    var row = 6

    @Key
    @Comment("点击延迟")
    var clickDelay = 200L

    var slots: MutableMap<String, MutableMap<ItemStack, IntArray>> = mutableMapOf()
        private set

    @Key("background")
    @Comment("背景图标")
    var backgroundSection: MemorySection = YamlConfiguration().apply {
        createSection("default", buildMap {
            put("slots", (0 until row * 9).joinToString(separator = ","))
            put("item", Material.GRAY_STAINED_GLASS_PANE.getItem().applyMeta { setDisplayName(" ") }.toSection())
        })
    }

    @Key("input")
    @Comment("工具输入槽，只能有一个")
    var inputSection: MemorySection = YamlConfiguration().apply {
        set("slots", "13")
        set("item", ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setName("${ChatColor.RED} 请放入待强化的物品")
        }.toSection())
    }

    @Key("material")
    @Comment("材料输入槽")
    var materialSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "29,30,31,32,33",
                "item" to ItemStack(Material.RED_STAINED_GLASS_PANE)
                    .applyMeta { setName("${ChatColor.RED} 请放入强化材料") }.toSection()
            )
        )
    }

    @Key("forge")
    @Comment("强化按钮")
    var forgeSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "40",
                "item" to ItemStack(Material.ANVIL).applyMeta { setName("${ChatColor.RED}无法强化") }.toSection()
            )
        )
    }

    @Key
    var forgeAllowed = "点击强化物品: &6{0} ￥"

    @Key("result")
    @Comment("强化产物输出槽")
    var resultSection: MemorySection = YamlConfiguration().apply {
        set("slots", "49")
        set("item", ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setName("${ChatColor.RED} 请放入待强化的物品")
        }.toSection())
    }


    override fun onLoaded(section: ConfigurationSection) {
        slots = mutableMapOf()
        readSlots("background", backgroundSection, slots)
        readSlot("input", inputSection, slots)
        readSlot("result", resultSection, slots)
        readSlots("material", materialSection, slots)
        readSlots("forge", forgeSection, slots)
    }

}