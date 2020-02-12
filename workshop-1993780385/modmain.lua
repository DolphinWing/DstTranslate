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

function table.translate(tbl, trans)
	for k,v in pairs(tbl) do
		if trans[v] then
			tbl[k] = trans[v]
		end
	end
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