-- reference from Chinese++(workshop-1418746242)
FONT_FILE_REGULAR = "fonts/normal.zip"
FONT_FILE_OUTLINE = "fonts/normal_outline.zip"
FONT_FILE_NUMBER = "fonts/number.zip"
FONT_PREFIX = "ya_fonts_"
FONT_REGULAR = FONT_PREFIX.."regular"
FONT_OUTLINE = FONT_PREFIX.."outline"
FONT_NUMBER = FONT_PREFIX.."number"

FONT_TABLE = {
	DEFAULTFONT = FONT_OUTLINE,
	DIALOGFONT = FONT_OUTLINE,
	TITLEFONT = FONT_OUTLINE,
	UIFONT = FONT_OUTLINE,
	BUTTONFONT = FONT_REGULAR,
	NEWFONT = FONT_REGULAR,
	NEWFONT_SMALL = FONT_REGULAR,
	NEWFONT_OUTLINE = FONT_OUTLINE,
	NEWFONT_OUTLINE_SMALL = FONT_OUTLINE,
	NUMBERFONT = FONT_NUMBER,
	SMALLNUMBERFONT = FONT_NUMBER,
	HEADERFONT = FONT_REGULAR,
	BODYTEXTFONT = FONT_OUTLINE,
	CODEFONT = FONT_REGULAR,
	TALKINGFONT = FONT_OUTLINE,
	TALKINGFONT_WORMWOOD = FONT_OUTLINE,
	CHATFONT = FONT_REGULAR,
	CHATFONT_OUTLINE = FONT_OUTLINE,
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
