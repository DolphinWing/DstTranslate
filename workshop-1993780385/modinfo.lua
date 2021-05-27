-- mod name
name = "正體中文包"
-- mod version
version = "1.0.7"
-- mod author
author = "DolphinWing and anetrlf2"
-- mod description in the game
description = "版本: "..version.." (v464835)\n\n"..
	"文字主要以Klei簡體中文文檔為主，輔以Klei正體中文文檔，"..
	"並參考Don't Starve中文維基，以及一些私心小修正。\n\n"..
	"當啟用「我想吃粉圓」的功能，會將原本遊戲字型換成「粉圓字型」。"..
	"如果你覺得更換字型之後的畫面文字實在太大，可以在設定中「使用放大鏡」調整字體縮放倍率，讓整體畫面較為平衡。\n\n"..
	"啟用正體中文包時會嘗試修正角色的對話。這將會在你建立的世界中預載為伺服器模組。"..
	"如果不喜歡這個功能，或者它拖慢了你的電腦效能，可以至設定中停用「修正人物對話」。\n\n"..
	"有任何問題或建議，歡迎至 Steam 工作坊上留言。"

forumthread = ""
-- DST API version
api_version = 10
-- mod icon
icon_atlas = "modicon.xml"
icon = "modicon.tex"
-- loading priority
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
	Option("停用", false),
	Option("啟用", true),
}

local size_def = {
	{description = "0.5x", data = 0.5},
	{description = "0.6x", data = 0.6},
	{description = "0.7x", data = 0.7},
	{description = "0.75x", data = 0.75},
	{description = "0.8x", data = 0.8},
	{description = "0.9x", data = 0.9},
	{description = "1.0x", data = 1.0},
	{description = "1.1x", data = 1.1},
	{description = "1.2x", data = 1.2},
	{description = "1.25x", data = 1.25},
	{description = "1.3x", data = 1.3},
	{description = "1.4x", data = 1.4},
	{description = "1.5x", data = 1.5},
}

configuration_options =
{
	Config("use_font", "我想吃粉圓", "使用粉圓字型取代遊戲內建字型", opt_def, false),
	Config("font_size", "使用放大鏡", "放大或縮小字體，避免和遊戲畫面不搭配", size_def, 1.0),
	--Config("font_size", "字體縮放倍率", "放大或縮小字體，避免和遊戲畫面不搭配", empty, 0),
	Config("as_server", "修正人物對話", "自動載入成伺服器模組（僅適用於自己建立的世界）", opt_def, true),
}
