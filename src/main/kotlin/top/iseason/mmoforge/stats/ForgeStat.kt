/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/19 下午9:17
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/17 下午7:45
 *
 */

/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/17 下午7:37
 *
 */

package top.iseason.mmoforge.stats

import io.lumine.mythic.lib.api.item.ItemTag
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem
import net.Indyuce.mmoitems.gui.edition.EditionInventory
import net.Indyuce.mmoitems.stat.data.random.RandomStatData
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.inventory.InventoryClickEvent
import top.iseason.mmoforge.config.MainConfig
import java.util.*

// 物品星级
object ForgeStat : ItemStat(
    "FORGE_ATTRIBUTE",
    Material.PAPER,
    "Forge Attribute",
    arrayOf("物品属性"),
    arrayOf("all")
) {
    override fun whenInitialized(config: Any?): RandomStatData {
        require(config is ConfigurationSection) { "Must specify a valid config section" }
        val star = config.getInt("star")
        require(star != 0) { "config \"star\" must be declared" }
        val attributeData = ForgeData(star)
        if (config.contains("max-refine")) {
            attributeData.maxRefine = config.getInt("max-refine")
        }
        if (config.contains("max-limit")) {
            attributeData.maxLimit = config.getInt("max-limit")
        }
        if (config.contains("max-forge")) {
            attributeData.maxForge = config.getInt("max-forge")
        }
        if (config.contains("gain-refine")) {
            attributeData.refineGain = MainConfig.getStatGain(config.getConfigurationSection("gain-refine")!!)
        }
        if (config.contains("gain-limit")) {
            attributeData.limitGain = MainConfig.getStatGain(config.getConfigurationSection("gain-limit")!!)
        }
        if (config.contains("gain-forge")) {
            attributeData.forgeGain = MainConfig.getStatGain(config.getConfigurationSection("gain-forge")!!)
        }
        return attributeData
    }

    override fun whenApplied(item: ItemStackBuilder, data: StatData) {
        item.addItemTag(getAppliedNBT(data))
    }

    override fun getAppliedNBT(data: StatData): ArrayList<ItemTag> {
        require(data is ForgeData) { "data type error ${data}" }
        return arrayListOf(ItemTag(nbtPath, data.toJson().toString()))
    }

    override fun getLoadedNBT(list: ArrayList<ItemTag>): StatData? {
        val tag = ItemTag.getTagAtPath(nbtPath, list) ?: return null
        val value = tag.value
        if (value !is String) return null
        return ForgeData.fromString(value)
    }

    override fun whenClicked(inventory: EditionInventory, event: InventoryClickEvent) {

    }

    override fun whenInput(inventory: EditionInventory, message: String, vararg info: Any?) {

    }

    override fun whenLoaded(mmoitem: ReadMMOItem) {
        //不需要实时读取
//        val itemTag = ItemTag.getTagAtPath(nbtPath, mmoitem.nbt, SupportedNBTTagValues.STRING) ?: return
//        val stat = getLoadedNBT(arrayListOf(itemTag)) ?: return
//        mmoitem.setData(this, stat)
    }

    override fun whenDisplayed(lore: MutableList<String>, data: Optional<RandomStatData>) {
        val forgeData = data.get() as ForgeData
        lore.add("${ChatColor.GRAY}物品强化属性: ")
        lore.add("${ChatColor.WHITE}星级: ${forgeData.star}")
        lore.add("${ChatColor.WHITE}最大精炼次数: ${forgeData.maxRefine}")
        lore.add("${ChatColor.WHITE}最大突破次数: ${forgeData.maxLimit}")
        lore.add("${ChatColor.WHITE}最大强化次数: ${forgeData.maxForge}")
        lore.add("${ChatColor.YELLOW}请从配置文件修改!")
    }

    override fun getClearStatData(): StatData = ForgeData(3)


}