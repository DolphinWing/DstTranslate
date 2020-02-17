name = "正體中文用戶端包"
version = "0.0.6"
author = "DolphinWing"
description = "版本: "..version.."\n\n文字主要以Klei簡體中文文檔為主，套用Klei正體中文文檔的翻譯，再加上一些作者的私心小修正。物品及名稱的翻譯，如Klei原始文檔沒有的話，會優先參考Don't Starve 中文維基"
forumthread = ""
api_version = 10
icon_atlas = "modicon.xml"
icon = "modicon.tex"
priority = 9999
-- Compatible with Don't Starve Together
dst_compatible = true
-- Compatible with both the base game and reign of giants
dont_starve_compatible = false
reign_of_giants_compatible = false
shipwrecked_compatible = false
--Some mods may crash or not work correctly until the game is restarted 
--after the mod is enabled/disabled
client_only_mod = true
server_only_mod = false
all_clients_require_mod = false
server_filter_tags = {"中文","Chinese","繁體中文","正體中文","繁體","台灣","Taiwan","Traditional Chinese"}

configuration_options =
{
}
