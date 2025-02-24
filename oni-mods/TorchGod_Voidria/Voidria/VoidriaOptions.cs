using Newtonsoft.Json;
using PeterHan.PLib.Options;

namespace Voidria
{
    [JsonObject(MemberSerialization.OptIn)]
    [ModInfo("https://github.com/DolphinWing/DstTranslate/tree/master/workshop-3413401611")]
    class VoidriaOptions
    {
        public static LocString GiftedAreaTitle = (LocString)"Treasure Room";
        public static LocString GiftedAreaTooltip = (LocString)"Enable treasure room";

        public static LocString TearOpenerTitle = (LocString)"Temporal Tear Opener";
        public static LocString TearOpenerTooltip = (LocString)"Open Temporal Tear by mysel";

        public static LocString IronVolcanoTitle = (LocString)"Iron Volcano";
        public static LocString IronVolcanoTooltip = (LocString)"Enable iron volcano";
 
        public static LocString OilReservoirTitle = (LocString)"Oil Reservoir";
        public static LocString OilReservoirTooltip = (LocString)"Enable oil reservoir";

        public static LocString CrittersCaveTitle = (LocString)"Critter Shalter";
        public static LocString CrittersCaveTooltip = (LocString)"Enable critter shalters";

        [Option("Voidria.VoidriaOptions.GiftedAreaTitle", "Voidria.VoidriaOptions.GiftedAreaTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_WORLDTRAITS")]
        [JsonProperty]
        public bool EnableGift { get; set; }

        [Option("Voidria.VoidriaOptions.TearOpenerTitle", "Voidria.VoidriaOptions.TearOpenerTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_WORLDTRAITS")]
        [JsonProperty]
        public bool EnableTearOpener { get; set; }

        [Option("Voidria.VoidriaOptions.IronVolcanoTitle", "Voidria.VoidriaOptions.IronVolcanoTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_GEYSERS")]
        [JsonProperty]
        public bool EnableIronVolcano { get; set; }

        [Option("Voidria.VoidriaOptions.OilReservoirTitle", "Voidria.VoidriaOptions.OilReservoirTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_GEYSERS")]
        [JsonProperty]
        public bool EnableOilReservoir { get; set; }

        [Option("Voidria.VoidriaOptions.CrittersCaveTitle", "Voidria.VoidriaOptions.CrittersCaveTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_LIFE")]
        [JsonProperty]
        public bool EnableCritters { get; set; }

        public VoidriaOptions()
        {
            EnableGift = true;
            EnableIronVolcano = true;
            EnableOilReservoir = true;
            EnableCritters = true;
            EnableTearOpener = false;
        }

        internal static VoidriaOptions GetInstance()
        {
            var options = POptions.ReadSettings<VoidriaOptions>();
            return options ?? new VoidriaOptions();
        }
    }
}
