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

-- save original method
local OldTranslateStringTable = GLOBAL.TranslateStringTable
-- override GLOBAL.TranslateStringTable method
GLOBAL.TranslateStringTable = function(...)
	LoadPOFile("dst_cht.po", "cht") -- load my translations
	OldTranslateStringTable(...) -- do translations
end

-- replace fonts
local useMyFont = GetConfig("use_font", false)
local function replaceFonts(assets)
	--unload previous fonts
	GLOBAL.TheSim:UnloadFont(FONT_REGULAR)
	GLOBAL.TheSim:UnloadFont(FONT_OUTLINE)
	GLOBAL.TheSim:UnloadPrefabs({FONT_PREFIX..modname})
	--register my fonts
	GLOBAL.TheSim:RegisterPrefab(FONT_PREFIX..modname, assets, {})
	GLOBAL.TheSim:LoadPrefabs({FONT_PREFIX..modname})
	--load fonts
	GLOBAL.TheSim:LoadFont(MODROOT..fontNormal, FONT_REGULAR)
	GLOBAL.TheSim:LoadFont(MODROOT..fontOutline, FONT_OUTLINE)
	--set fallback fonts
	GLOBAL.TheSim:SetupFontFallbacks(FONT_REGULAR, GLOBAL.DEFAULT_FALLBACK_TABLE)
	GLOBAL.TheSim:SetupFontFallbacks(FONT_OUTLINE, GLOBAL.DEFAULT_FALLBACK_TABLE_OUTLINE)
	--replace all with our fonts
	for k,v in pairs(FONT_TABLE) do
		GLOBAL[k]=v
	end
end

if useMyFont and fileExists(MODROOT..fontNormal) then
	local Assets = {}
	table.insert(Assets, GLOBAL.Asset("FONT", MODROOT..fontNormal))
	table.insert(Assets, GLOBAL.Asset("FONT", MODROOT..fontOutline))
	
	local OldStart=GLOBAL.Start
	GLOBAL.Start=function()
		replaceFonts(Assets)
		OldStart()
	end
	
	local OldRegisterPrefabs=GLOBAL.ModManager.RegisterPrefabs
	GLOBAL.ModManager.RegisterPrefabs = function(...)
		OldRegisterPrefabs(...)
		replaceFonts(Assets)
	end
end

local fontRatio = GetConfig("font_size", 1.0)
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
	local opts=self:GetGraphicsOptions()
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
	fixTaskSet()       -- fix preset tasks
	fixStartLocation() -- fix preset tasks
	fixPresetLevel()   -- fix preset tasks
	enableAsServerMod(self)
end)
