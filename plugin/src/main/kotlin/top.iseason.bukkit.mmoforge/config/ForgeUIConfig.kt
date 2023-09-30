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
import top.iseason.bukkit.mmoforge.uitls.item
import top.iseason.bukkit.mmoforge.uitls.setName
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
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
            put("item", Material.GRAY_STAINED_GLASS_PANE.item.applyMeta { setDisplayName(" ") }.toSection())
        })
    }

    @Key("input")
    @Comment("工具输入槽，只能有一个")
    var inputSection: MemorySection = YamlConfiguration().apply {
        set("slots", "13")
        set("item", Material.RED_STAINED_GLASS_PANE.item.applyMeta {
            setName("${ChatColor.RED} 请放入待强化的物品")
        }.toSection())
    }

    @Key("material")
    @Comment("材料输入槽")
    var materialSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "29,30,31,32,33",
                "item" to Material.RED_STAINED_GLASS_PANE.item
                    .applyMeta { setName("${ChatColor.RED} 请放入强化材料") }.toSection()
            )
        )
    }

    @Key("default-forge")
    @Comment("", "默认不放入东西显示的强化按钮")
    var forgeSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "40",
                "item" to Material.ANVIL.item.applyMeta { setName("${ChatColor.RED}无法强化") }.toSection()
            )
        )
    }

    @Key("allow-forge")
    @Comment(
        "", "可以强化时显示的物品",
        "{gold} 是需要的金币的占位符",
        "{chance}是强化成功率"
    )
    var allowForgeSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "40",
                "item" to Material.ANVIL.item.applyMeta {
                    setName("点击强化物品: &6{gold} ￥ &7概率: &b{chance}")
                }.toSection()
            )
        )
    }

    @Key("result")
    @Comment("强化产物输出槽")
    var resultSection: MemorySection = YamlConfiguration().apply {
        set("slots", "49")
        set("item", Material.RED_STAINED_GLASS_PANE.item.applyMeta {
            setName("${ChatColor.RED} 请放入待强化的物品")
        }.toSection())
    }

    override fun onLoaded(section: ConfigurationSection) {
        slots = mutableMapOf()
        readSlots("background", backgroundSection, slots)
        readSlot("input", inputSection, slots)
        readSlot("result", resultSection, slots)
        readSlots("material", materialSection, slots)
        readSlots("default-forge", forgeSection, slots)
        readSlots("allow-forge", allowForgeSection, slots)
    }

}