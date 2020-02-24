-- reference from Chinese++(workshop-1418746242)
fontNormal = "fonts/normal.zip"
fontOutline = "fonts/normal_outline.zip"

FONT_TABLE = {
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

task_set_path = GLOBAL.STRINGS.UI.CUSTOMIZATIONSCREEN.TASKSETNAMES
task_set_name = {
	default = "DEFAULT",
	classic = "CLASSIC",
	cave_default = "CAVE_DEFAULT",
	lavaarena_taskset = "LAVA_ARENA",
	quagmire_taskset = "QUAGMIRE",
}

start_location_path = GLOBAL.STRINGS.UI.SANDBOXMENU
start_location_name = {
	default = "DEFAULTSTART",
	plus = "PLUSSTART",
	darkness = "DARKSTART",
	caves = "CAVESTART",
	lavaarena = "DEFAULTSTART",
	quagmire_startlocation = "DEFAULTSTART",
}

level_name_path = GLOBAL.STRINGS.UI.CUSTOMIZATIONSCREEN.PRESETLEVELS
level_desc_path = GLOBAL.STRINGS.UI.CUSTOMIZATIONSCREEN.PRESETLEVELDESC

function fileExists(name)
	local f=GLOBAL.io.open(name,"r")
	if f~=nil then
		GLOBAL.io.close(f) 
		return true 
	else 
		return false 
	end
end

function GetConfig(name, default)
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

function getVal(fn, path)
	if fn == nil or type(fn)~="function" then return end
	local val = fn
	for entry in path:gmatch("[^%.]+") do
		local i = 1
		while true do
			local name, value = GLOBAL.debug.getupvalue(val, i)
			if name == entry then
				val = value
				break
			elseif name == nil then
				return
			end
			i = i + 1
		end
	end
	return val, i
end
