/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2023/6/10 下午7:56
 *
 */

package top.iseason.bukkit.mmoforge.config

import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key

@Key
@FilePath("lang.yml")
object Lang : top.iseason.bukkittemplate.config.Lang() {

    @Comment("", "消息冷却时间, 单位毫秒")
    var cooldown = 1000L

    var ui_forge_no_gold = "&6你没有足够的金币!"

    @Comment("占位符{0}原来的等级，{1}新的等级")
    var ui_forge_success = "&a强化成功!"

    var ui_break_no_gold = "&6你没有足够的金币!"

    @Comment("占位符{0}原来的等级，{1}新的等级")
    var ui_break_success = "&a突破成功!"

    var ui_refine_no_gold = "&6你没有足够的金币!"

    @Comment("占位符{0}原来的等级，{1}新的等级")
    var ui_refine_success = "&a精炼成功!"

}