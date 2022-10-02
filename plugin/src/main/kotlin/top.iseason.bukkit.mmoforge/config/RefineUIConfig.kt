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

@FilePath("ui/refine.yml")
object RefineUIConfig : SimpleYAMLConfig() {

    @Key
    @Comment("ui标题")
    var title = "物品精炼"

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

    @Key("tool")
    @Comment("工具输入槽，只能有一个")
    var toolSection: MemorySection = YamlConfiguration().apply {
        set("slots", "19")
        set("item", ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setName("${ChatColor.RED} 请放入待精炼的物品")
        }.toSection())
    }


    @Key("material")
    @Comment("材料输入槽，只能有一个")
    var materialSection: MemorySection = YamlConfiguration().apply {
        set("slots", "21")
        set(
            "item", ItemStack(Material.RED_STAINED_GLASS_PANE)
                .applyMeta { setName("${ChatColor.RED} 请放入相同的物品，将会被消耗") }.toSection()
        )
    }

    @Key("refine")
    @Comment("精炼按钮")
    var refineSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "23",
                "item" to ItemStack(Material.ANVIL).applyMeta { setName("${ChatColor.RED}无法精炼") }.toSection()
            )
        )
    }

    @Key("result")
    @Comment("精炼产物输出槽")
    var resultSection: MemorySection = YamlConfiguration().apply {
        set("slots", "25")
        set("item", ItemStack(Material.RED_STAINED_GLASS_PANE).applyMeta {
            setName("${ChatColor.RED} 请放入待精炼的物品")
        }.toSection())
    }

    @Key
    var refineAllowed = "点击精炼物品: &6{0} ￥"

    override fun onLoaded(section: ConfigurationSection) {
        slots = mutableMapOf()
        readSlots("background", backgroundSection, slots)
        readSlot("tool", toolSection, slots)
        readSlot("result", resultSection, slots)
        readSlot("material", materialSection, slots)
        readSlots("refine", refineSection, slots)
    }

}