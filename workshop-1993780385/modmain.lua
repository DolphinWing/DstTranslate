--[[
    author: DolphinWing
    https://github.com/DolphinWing/DSTTranslate/tree/master/workshop-1993780385
    https://steamcommunity.com/sharedfiles/filedetails/?id=1993780385
]]

local this_mod = "workshop-1993780385" -- steam workshop id

modimport("utils.lua")

--local pofilename = "dst_cht.po"
--LoadPOFile(pofilename, "cht")
--GLOBAL.TranslateStringTable( GLOBAL.STRINGS )

--[[if not GLOBAL.TheNet:IsDedicated() or GLOBAL.TheNet:GetServerIsClientHosted() then
	GLOBAL.KnownModIndex:LoadModConfigurationOptions(this_mod, true)
end]]

local useMyPo = GetConfig("replace_po", true)
if useMyPo then
	-- save original method
	local OldTranslateStringTable = GLOBAL.TranslateStringTable
	-- override GLOBAL.TranslateStringTable method
	GLOBAL.TranslateStringTable = function(...)
		-- load offical translation first, take traditional chinese first
		if fileExists("scripts/languages/chinese_t.po") then
			GLOBAL.LanguageTranslator:LoadPOFile("scripts/languages/chinese_t.po", "chs")
		end
		LoadPOFile("dst_cht.po", "cht") -- load my translations
		OldTranslateStringTable(...) -- do translations
		--for k, v in pairs(GLOBAL.STRINGS.ACTIONS) do
		--	if GLOBAL.ACTIONS[k] then GLOBAL.ACTIONS[k].str = v end
		--end
	end
end

--[[ dedicated server, no need to load local configuration change
if GLOBAL.TheNet:IsDedicated() then
	return
end ]]

-- replace fonts
local useMyFont = GetConfig("use_font", false)
local function replaceFonts(assets)
	--unload previous fonts
	GLOBAL.TheSim:UnloadFont(FONT_REGULAR)
	GLOBAL.TheSim:UnloadFont(FONT_OUTLINE)
	GLOBAL.TheSim:UnloadFont(FONT_NUMBER)
	GLOBAL.TheSim:UnloadPrefabs({FONT_PREFIX..modname})
	--register my fonts
	GLOBAL.TheSim:RegisterPrefab(FONT_PREFIX..modname, assets, {})
	GLOBAL.TheSim:LoadPrefabs({FONT_PREFIX..modname})
	--load fonts
	GLOBAL.TheSim:LoadFont(MODROOT..FONT_FILE_REGULAR, FONT_REGULAR)
	GLOBAL.TheSim:LoadFont(MODROOT..FONT_FILE_OUTLINE, FONT_OUTLINE)
	GLOBAL.TheSim:LoadFont(MODROOT..FONT_FILE_NUMBER, FONT_NUMBER)
	--set fallback fonts
	GLOBAL.TheSim:SetupFontFallbacks(FONT_REGULAR, GLOBAL.DEFAULT_FALLBACK_TABLE)
	GLOBAL.TheSim:SetupFontFallbacks(FONT_OUTLINE, GLOBAL.DEFAULT_FALLBACK_TABLE_OUTLINE)
	GLOBAL.TheSim:SetupFontFallbacks(FONT_NUMBER, GLOBAL.DEFAULT_FALLBACK_TABLE_OUTLINE)
	--replace all with our fonts
	for k, v in pairs(FONT_TABLE) do
		GLOBAL[k] = v
	end
end

local fontRatio = GetConfig("font_size", 1.0)

if useMyFont and fileExists(MODROOT..FONT_FILE_REGULAR) then
    fontRatio = fontRatio * .9

	local assets = {}
	table.insert(assets, GLOBAL.Asset("FONT", MODROOT..FONT_FILE_REGULAR))
	table.insert(assets, GLOBAL.Asset("FONT", MODROOT..FONT_FILE_OUTLINE))
	table.insert(assets, GLOBAL.Asset("FONT", MODROOT..FONT_FILE_NUMBER))
	
	local OldStart = GLOBAL.Start
	GLOBAL.Start = function()
		replaceFonts(assets)
		OldStart()
	end
	
	local OldRegisterPrefabs = GLOBAL.ModManager.RegisterPrefabs
	GLOBAL.ModManager.RegisterPrefabs = function(...)
		OldRegisterPrefabs(...)
		replaceFonts(assets)
	end
end

--resize widget text size
AddClassPostConstruct("widgets/text", function(self)
    if self.size then
	    self:SetSize( (self.size * fontRatio) )
    end
    function self:SetSize(sz)
	    local sz_ = sz * fontRatio
	    self.inst.TextWidget:SetSize(sz_)
	    self.size = sz_
    end
end)

local function fixGraphicSmallTexture(self)
	-- load graphics options
	local opts = self:GetGraphicsOptions()
    -- if small texture is enabled
	if opts and opts:IsSmallTexturesMode() then
		-- disable small texture
		opts:SetSmallTexturesMode(false)
	end
end

local function fixTaskSet()
	local tasksets = GLOBAL.require("map/tasksets")
    local val, i = getVal(tasksets.GetGenTaskLists, "taskgrouplist")
	for k, v in pairs(val) do
		if task_set_name[k] then
			v.name = task_set_path[task_set_name[k]]
		end
	end
end

local function fixStartLocation()
    local startlocations = GLOBAL.require("map/startlocations")
	local val, i = getVal(startlocations.GetGenStartLocations, "startlocations")
	for k, v in pairs(val) do
		if start_location_name[k] then
			v.name = start_location_path[start_location_name[k]]
		end
	end
end

local function fixPresetLevel()
	local levels = GLOBAL.require("map/levels")
	local val, i = getVal(levels.GetLevelList, "levellist")
	for _, v in pairs(val) do
		for i, vv in ipairs(v) do
			if level_name_path[vv.id] then
				vv.name = level_name_path[vv.id]
				vv.desc = level_desc_path[vv.id]
			end
		end
	end
end

local function enableAsServerMod(self)
    local loasAsServer = GetConfig("as_server", true)
	-- save original method
	local OldGetEnabledServerModNames = GLOBAL.ModManager.GetEnabledServerModNames
	-- override GLOBAL.ModManager.GetEnabledServerModNames
	GLOBAL.ModManager.GetEnabledServerModNames = function(self)
		-- load current enabled mods
		local server_mods = OldGetEnabledServerModNames(self)
		if GLOBAL.IsNotConsole() and loasAsServer then
			--load translation mod as server mod
			table.insert(server_mods, this_mod)
		end
		return server_mods
	end
end

-- delay load init features, refs. Chinese++(workshop-1418746242)
AddGlobalClassPostConstruct("frontend", "FrontEnd", function(self)
	fixGraphicSmallTexture(self)
	if useMyPo then
		fixTaskSet()       -- fix preset tasks
		fixStartLocation() -- fix preset tasks
		fixPresetLevel()   -- fix preset tasks
		enableAsServerMod(self)
	end
end)

local function fixClockHudControlFontSize()
	AddClassPostConstruct("widgets/uiclock", function(self)
		if self._text then
			--self._text:SetFont(FONT_TABLE.NUMBERFONT)
			self._text:SetSize(25 / fontRatio) --original 33 / 1
		end
		--if self._moonanim and self._moonanim.moontext then
		--	self._moonanim.moontext:SetFont(FontNames.NUMBERFONT)
		--end
	end)

	AddClassPostConstruct("widgets/badge", function(self)
		if self.num then
			self.num:SetFont(FONT_TABLE.NUMBERFONT)
			self.num:SetSize(30 / fontRatio)
		end
		if self.maxnum then
			self.maxnum:SetFont(FONT_TABLE.NUMBERFONT)
			self.maxnum:SetSize(30 / fontRatio)
		end
	end)
end

local function fixRecipeHudControlFontSize()
	--AddClassPostConstruct("widgets/recipepopup", function(self)
	--	if self.desc then
	--		self.desc:SetSize(30 / fontRatio) --original 33 or 30
	--		self.desc:SetRegionSize(64 * 3 + 30, 90)
	--	end
	--end)

	AddClassPostConstruct("widgets/ingredientui", function(self)
		if self.quant then
			--self.quant:SetFont(FontNames.NUMBERFONT)
			self.quant:SetSize(26 / fontRatio)
		end
	end)
end

local function fixInventoryFontSize()
	AddClassPostConstruct("widgets/itemtile", function(self)
		if self.quantity then
			--self.quantity:SetFont(FontNames.NUMBERFONT)
			self.quantity:SetSize(42 / fontRatio)
		end
		if self.percent then
			--self.percent:SetFont(FontNames.NUMBERFONT)
			self.percent:SetSize(42 / fontRatio)
		end
	end)

	AddClassPostConstruct("widgets/itemslot", function(self)
		if self.label then
			--self.label:SetFont(FontNames.NUMBERFONT)
			self.label:SetSize(26 / fontRatio)
		end
	end)
end

local function fixLoadingWidgetFontSize()
	AddClassPostConstruct("widgets/redux/loadingwidget", function(self)
		if self.loading_widget then
			self.loading_widget:SetFont(FONT_TABLE.UIFONT) --HEADERFONT
			--self.loading_widget:SetSize(30) --35
			self.loading_widget:SetRegionSize(144, 44)
		end
	end)
end

local function fixMainScreenButtonFontSize()
	AddClassPostConstruct("screens/multiplayermainscreen", function(self)
		if self.submenu then
			self.submenu:SetTextSize(20 / fontRatio)
		end
	end)
	AddClassPostConstruct("screens/redux/multiplayermainscreen", function(self)
		if self.submenu then
			self.submenu:SetTextSize(20 / fontRatio)
		end
	end)
end

local function fixCountDownWidgetFontSize()
	AddClassPostConstruct("widgets/countdown", function(self)
		if self.daysuntiltext then
			--self.daysuntiltext:SetFont(FontNames.NUMBERFONT)
			self.daysuntiltext:SetSize(30 / fontRatio)
		end
	end)

	AddClassPostConstruct("widgets/countdownbeta", function(self)
		local dayTextSize = 35
		if self.title then
			dayTextSize = 30 --scale down when we have title
			--self.title:SetFont(FontNames.NUMBERFONT)
			self.title:SetSize(35 / fontRatio)
		end
		if self.daysuntiltext then
			--self.daysuntiltext:SetFont(FontNames.NUMBERFONT)
			self.daysuntiltext:SetSize(dayTextSize / fontRatio)
		end
		if self.title2 then
			--self.title2:SetFont(FontNames.NUMBERFONT)
			self.title2:SetSize(25 / fontRatio)
		end
	end)
end

local function fixMiscGuiFontSize()
	AddClassPostConstruct("screens/redeemdialog", function(self)
		if self.fineprint then
			self.fineprint:SetSize(16 / fontRatio) --original 17
		end
	end)

	AddClassPostConstruct("screens/pausescreen", function(self)
		if self.subtitle then
			self.subtitle:SetSize(16 / fontRatio) --original 16
		end
	end)
end

local function fixMiscWidgetFontSize()
	AddClassPostConstruct("widgets/demotimer", function(self)
		if self.text then
			--self.text:SetFont(FontNames.NUMBERFONT)
			self.text:SetSize(30 / fontRatio)
		end
	end)

	AddClassPostConstruct("widgets/skincollector", function(self)
		if self.text then
			self.text:SetSize(30 / fontRatio) --original 35
		end
	end)
end

local function fixIntentPickerSize()
	AddClassPostConstruct("widgets/redux/intentionpicker", function(self)
		if self.description then
			self.description:SetRegionSize(520, 320) --500, 280
			self.description:SetPosition(0, -420) --0, 380
			self.description:SetSize(26 / fontRatio) --original 35
		end
	end)
end

--override some text size
if useMyFont and fileExists(MODROOT..FONT_FILE_REGULAR) then
	fixClockHudControlFontSize()
	fixRecipeHudControlFontSize()
	fixInventoryFontSize()
	fixLoadingWidgetFontSize()
	--fixMainScreenButtonFontSize()
	fixMiscGuiFontSize()
	fixMiscWidgetFontSize()
	--fixIntentPickerSize()
	fixCountDownWidgetFontSize()
end
