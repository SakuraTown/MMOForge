/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/10/1 下午7:44
 *
 */

package top.iseason.bukkit.mmoforge.config

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import top.iseason.bukkit.mmoforge.uitls.item
import top.iseason.bukkit.mmoforge.uitls.setName
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.applyMeta
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils.toSection

@FilePath("ui/break_through.yml")
object BreakUIConfig : SimpleYAMLConfig() {
    @Key
    @Comment("ui标题")
    var title = "物品突破"

    @Key
    @Comment("ui行数")
    var row = 6

    @Key
    @Comment("点击延迟")

    var clickDelay = 200L

    var slots: MutableMap<String, MutableMap<ItemStack, IntArray>> = mutableMapOf()
        private set

    @Key("background")
    @Comment("", "背景图标")
    var backgroundSection: MemorySection = YamlConfiguration().apply {
        createSection("default", buildMap {
            put("slots", (0 until row * 9).joinToString(separator = ","))
            put("item", Material.GRAY_STAINED_GLASS_PANE.item.applyMeta { setDisplayName(" ") }.toSection())
        })
    }

    @Key("input")
    @Comment("", "工具输入槽，只能有一个")
    var inputSection: MemorySection = YamlConfiguration().apply {
        set("slots", "13")
        set("item", Material.RED_STAINED_GLASS_PANE.item.applyMeta {
            setDisplayName("&c请先放入需要突破的物品")
        }.toSection())
    }

    @Key("default-materials")
    @Comment("", "默认材料输入槽")
    var materialsSection: MemorySection = YamlConfiguration().apply {
        createSection("default", buildMap {
            put("slots", "30,31,32")
            put("item", Material.RED_STAINED_GLASS_PANE.item.applyMeta { setDisplayName("&c请先放入需要突破的物品") }
                .toSection())
        })
    }

    @Key("allow-materials")
    @Comment("", "接受材料时的材料输入槽，{0} 为需要的物品的类型名称；{1} 为需要的物品的名字")
    var allowMaterialsSection: MemorySection = YamlConfiguration().apply {
        createSection("default", buildMap {
            put("slots", "30,31,32")
            put("item", Material.RED_STAINED_GLASS_PANE.item.applyMeta { setDisplayName("&6请放入:&f{0} &f{1}") }
                .toSection())
        })
    }

    @Key("output")
    @Comment("", "工具输出槽，只能有一个")
    var outputSection: MemorySection = YamlConfiguration().apply {
        set("slots", "49")
        set("item", Material.RED_STAINED_GLASS_PANE.item.applyMeta {
            setDisplayName("&c请先放入需要突破的物品")
        }.toSection())
    }

    @Key("default-break")
    @Comment("", "默认显示的突破按钮，无法突破")
    var breakThroughSection: MemorySection = YamlConfiguration().apply {
        createSection("default", buildMap {
            put("slots", "40")
            put("item", Material.ANVIL.item.applyMeta {
                setDisplayName("&c无法突破")
            }.toSection())
        })
    }

    @Key("allow-break")
    @Comment("", "可以突破时的突破按钮", "{gold} 是需要的金币的占位符", "{chance}是强化成功率")
    var allowBreakThroughSection: MemorySection = YamlConfiguration().apply {
        createSection(
            "default", mutableMapOf(
                "slots" to "40",
                "item" to Material.ANVIL.item.applyMeta { setName("点击突破物品: &6{gold} ￥ &7概率: &b{chance}") }
                    .toSection()
            )
        )
    }

    override fun onLoaded(section: ConfigurationSection) {
        slots = mutableMapOf()
        readSlots("background", backgroundSection, slots)
        readSlots("default-materials", materialsSection, slots)
        readSlots("allow-materials", allowMaterialsSection, slots)
        readSlots("default-break", breakThroughSection, slots)
        readSlots("allow-break", allowBreakThroughSection, slots)
        readSlot("input", inputSection, slots)
        readSlot("output", outputSection, slots)

    }

    /**
     * 读取多个槽
     */
    fun readSlots(
        type: String,
        section: ConfigurationSection,
        slotMap: MutableMap<String, MutableMap<ItemStack, IntArray>>
    ) {
        section.getKeys(false).forEach {
            readSlot(type, section.getConfigurationSection(it)!!, slotMap)
        }
    }

    /**
     * 读取单个槽
     */
    fun readSlot(
        type: String,
        section: ConfigurationSection,
        slotMap: MutableMap<String, MutableMap<ItemStack, IntArray>>
    ) {
        val slotsStr = section.getString("slots") ?: return
        val slots = slotsStr.trim().split(',').mapNotNull { runCatching { it.toInt() }.getOrNull() }.toIntArray()
        val itemSection = section.getConfigurationSection("item") ?: return
        val item = ItemUtils.fromSection(itemSection) ?: return
        val typeSlots = slotMap.computeIfAbsent(type) { mutableMapOf() }
        typeSlots[item] = slots
    }

}