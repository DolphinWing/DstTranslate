--local pofilename = "DST_cht.po"
--LoadPOFile(pofilename, "cht")
--GLOBAL.TranslateStringTable( GLOBAL.STRINGS )

local this_mod = "workshop-1993780385" -- steam workshop id

-- save original method
local OldTranslateStringTable = GLOBAL.TranslateStringTable
-- override GLOBAL.TranslateStringTable method
GLOBAL.TranslateStringTable = function(...)
	LoadPOFile("dst_cht.po", "cht") -- load my translations
	OldTranslateStringTable(...) -- do translations
end

local function GetConfig(name, default)
	if conf and type(conf)=="table" then
		for _,v in pairs(conf) do
			if v.name==name and v.saved then
				return v.saved
			end
		end
	end
	local data = GetModConfigData(name)
	if data == nil then return default end
	return data
end

function fileExists(name)
	local f=GLOBAL.io.open(name,"r")
	if f~=nil then
		GLOBAL.io.close(f) 
		return true 
	else 
		return false 
	end
end

-- replace fonts
local useMyFont = GetConfig("use_font", false)
local fontRatio = GetConfig("font_size", 1.0)
local fontNormal = "fonts/normal.zip"
local fontOutline = "fonts/normal_outline.zip"

if useMyFont and fileExists(MODROOT..fontNormal) then
	local Assets = {}
	table.insert(Assets, GLOBAL.Asset("FONT", MODROOT..fontNormal))
	table.insert(Assets, GLOBAL.Asset("FONT", MODROOT..fontOutline))

	local FONT_TABLE = {
		DEFAULTFONT = "myfont_outline",
		DIALOGFONT = "myfont_outline",
		TITLEFONT = "myfont_outline",
		UIFONT = "myfont_outline",
		BUTTONFONT = "myfont",
		NEWFONT = "myfont",
		NEWFONT_SMALL = "myfont",
		NEWFONT_OUTLINE = "myfont_outline",
		NEWFONT_OUTLINE_SMALL = "myfont_outline",
		NUMBERFONT = "myfont_outline",
		SMALLNUMBERFONT = "myfont_outline",
		BODYTEXTFONT = "myfont_outline",
		CODEFONT = "myfont",
		TALKINGFONT = "myfont_outline",
		TALKINGFONT_WORMWOOD = "myfont_outline",
		CHATFONT = "myfont",
		HEADERFONT = "myfont",
		CHATFONT_OUTLINE = "myfont_outline",
	}
	
	local function replaceFonts()
		--unload previous fonts
		GLOBAL.TheSim:UnloadFont("myfont")
		GLOBAL.TheSim:UnloadFont("myfont_outline")
		GLOBAL.TheSim:UnloadPrefabs({"myfonts_"..modname})
		--register my fonts
		GLOBAL.TheSim:RegisterPrefab("myfonts_"..modname, Assets, {})
		GLOBAL.TheSim:LoadPrefabs({"myfonts_"..modname})
		--load fonts
		GLOBAL.TheSim:LoadFont(MODROOT..fontNormal, "myfont")
		GLOBAL.TheSim:LoadFont(MODROOT..fontOutline, "myfont_outline")
		--set fallback fonts
		GLOBAL.TheSim:SetupFontFallbacks("myfont", GLOBAL.DEFAULT_FALLBACK_TABLE)
		GLOBAL.TheSim:SetupFontFallbacks("myfont_outline", GLOBAL.DEFAULT_FALLBACK_TABLE_OUTLINE)
		--replace all with our fonts
		for k,v in pairs(FONT_TABLE) do
			GLOBAL[k]=v
		end
	end
	
	local OldStart=GLOBAL.Start
	GLOBAL.Start=function()
		replaceFonts()
		OldStart()
	end
	
	local OldRegisterPrefabs=GLOBAL.ModManager.RegisterPrefabs
	GLOBAL.ModManager.RegisterPrefabs = function(...)
		OldRegisterPrefabs(...)
		replaceFonts()
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
end

-- delay load init features, refs. Chinese++(workshop-1418746242)
AddGlobalClassPostConstruct("frontend", "FrontEnd", function(self)
	-- load graphics options
	local opts=self:GetGraphicsOptions()
    -- if small texture is enabled
	if opts and opts:IsSmallTexturesMode() then
		-- disable small texture
		opts:SetSmallTexturesMode(false)
	end

	-- save original method
	local OldGetEnabledServerModNames = GLOBAL.ModManager.GetEnabledServerModNames
	-- override GLOBAL.ModManager.GetEnabledServerModNames
	GLOBAL.ModManager.GetEnabledServerModNames=function(self)
		-- load current enabled mods
		local server_mods = OldGetEnabledServerModNames(self)
		if GLOBAL.IsNotConsole() then
			--load translation mod as server mod
			table.insert(server_mods, this_mod)
		end
		return server_mods
	end
end)