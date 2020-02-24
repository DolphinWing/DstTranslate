name = "正體中文用戶端包"
version = "0.2.1"
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

--[[
The style is from Re-Gorge-itated: workshop-1918927570
]]
local function Option(desc, data, hover)
	return {
		description = desc,
		data = data,
		hover = hover or "",
	}
end

local function Config(name, label, hover, options, default)
	return {
		name = name,
		label = label,
		hover = hover or "",
		options = options,
		default = default
	}
end

local empty = {{description = "", data = 0}}

local opt_def = {
	Option("啟用", true),
	Option("停用", false),
}

local size_def = { 
	{description = "0.5x", data = 0.5}, 
	{description = "0.6x", data = 0.6}, 
	{description = "0.7x", data = 0.7}, 
	{description = "0.8x", data = 0.8}, 
	{description = "0.9x", data = 0.9}, 
	{description = "1.0x", data = 1.0}, 
	{description = "1.2x", data = 1.2}, 
	{description = "1.5x", data = 1.5}, 
}

--local function CheckFonts()
--	if PLATFORM == "LINUX_STEAM" then
--	if fileexists(MODROOT.."fonts/normal.zip") then
--		--return Config("use_font", "取代字型", "用 Noto 取代遊戲內建字型", opt_def, false)
--		return Config("font_size", "字型縮放倍率", "放大或縮小字體，避免和遊戲畫面不搭配", size_def, 1.0)
--	else
--		return Config("font_size", "字型縮放倍率", "放大或縮小字體，避免和遊戲畫面不搭配", empty, 0)
--	end
--end

configuration_options =
{
	--Config("use_font", "取代字型", "用 Noto 取代遊戲內建字型", opt_def, false),
	--Config("font_size", "字型縮放倍率", "放大或縮小字體，避免和遊戲畫面不搭配", size_def, 1.0),
	--Config("font_size", "字型縮放倍率", "放大或縮小字體，避免和遊戲畫面不搭配", empty, 0),
}
