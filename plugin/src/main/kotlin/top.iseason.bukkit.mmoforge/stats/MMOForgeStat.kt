/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/19 下午9:17
 *
 */

package top.iseason.bukkit.mmoforge.stats

import io.lumine.mythic.lib.api.item.ItemTag
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem
import net.Indyuce.mmoitems.gui.edition.EditionInventory
import net.Indyuce.mmoitems.stat.type.ItemStat
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.inventory.InventoryClickEvent
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.uitls.getForgeData
import java.util.*

// 物品星级
object MMOForgeStat : ItemStat<MMOForgeData, MMOForgeData>(
    "FORGE_ATTRIBUTE",
    Material.PAPER,
    "Forge Attribute",
    arrayOf("物品属性"),
    arrayOf("all")
) {
    override fun whenInitialized(config: Any?): MMOForgeData {
        require(config is ConfigurationSection) { "Must specify a valid config section" }
        val star = config.getInt("star")
        require(star != 0) { "config \"star\" must be declared" }
        val attributeData = MMOForgeData(star)
        if (config.contains("refine")) {
            attributeData.refine = config.getInt("refine")
        }
        if (config.contains("limit")) {
            attributeData.limit = config.getInt("limit")
        }
        if (config.contains("forge")) {
            attributeData.forge = config.getInt("forge")
        }
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
        if (config.contains("limit-type")) {
            val section = config.getConfigurationSection("limit-type")!!
            val linkedHashMap = LinkedHashMap<Int, List<String>>()
            section.getKeys(false).forEach {
                linkedHashMap[it.toInt()] = section.getStringList(it)
            }
            attributeData.limitType = linkedHashMap
        }
        return attributeData
    }

    override fun getAppliedNBT(p0: MMOForgeData): ArrayList<ItemTag> {
        return arrayListOf(ItemTag(nbtPath, p0.toJson().toString()))
    }

    override fun whenApplied(item: ItemStackBuilder, statData: MMOForgeData) {
        try {
            item.lore.insert(this.path, MainConfig.getItemLore(statData))
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        MainConfig.getItemLore(statData).forEach {
////            item.lore.lore.add(it)
//            item.lore.end(it)
//        }
        item.addItemTag(getAppliedNBT(statData))
    }

    override fun getLoadedNBT(list: ArrayList<ItemTag>): MMOForgeData? {
        val tag = ItemTag.getTagAtPath(nbtPath, list) ?: return null
        val value = tag.value
        if (value !is String) return null
        return MMOForgeData.fromString(value)
    }

    override fun whenClicked(inventory: EditionInventory, event: InventoryClickEvent) {

    }

    override fun whenInput(inventory: EditionInventory, message: String, vararg info: Any?) {

    }

    override fun whenLoaded(mmoitem: ReadMMOItem) {
        val forgeData = mmoitem.nbt.getForgeData() ?: return
        mmoitem.setData(MMOForgeStat, forgeData)
    }

    override fun whenDisplayed(lore: MutableList<String>, data: Optional<MMOForgeData>) {
        data.ifPresent {
            lore.add("${ChatColor.GRAY}物品强化属性: ")
            lore.add("${ChatColor.WHITE}星级: ${it.star}")
            lore.add("${ChatColor.WHITE}最大精炼次数: ${it.maxRefine}")
            lore.add("${ChatColor.WHITE}最大突破次数: ${it.maxLimit}")
            lore.add("${ChatColor.WHITE}最大强化次数: ${it.maxForge}")
            lore.add("${ChatColor.YELLOW}请从配置文件修改!")
        }
    }

    override fun getClearStatData(): MMOForgeData = MMOForgeData(3)


}