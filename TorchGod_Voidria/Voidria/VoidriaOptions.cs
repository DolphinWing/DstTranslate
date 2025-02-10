using Newtonsoft.Json;
using PeterHan.PLib.Options;

namespace Voidria
{
    [JsonObject(MemberSerialization.OptIn)]
    [ModInfo("https://github.com/DolphinWing/DstTranslate/tree/master/workshop-3413401611")]
    class VoidriaOptions
    {
        public static LocString IronVolcanoTitle = (LocString)"Iron Volcano";
        public static LocString IronVolcanoTooltip = (LocString)"Enable iron volcano";
 
        public static LocString OilReservoirTitle = (LocString)"Oil Reservoir";
        public static LocString OilReservoirTooltip = (LocString)"Enable oil reservoir";

        [Option("Voidria.VoidriaOptions.IronVolcanoTitle", "Voidria.VoidriaOptions.IronVolcanoTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_GEYSERS")]
        [JsonProperty]
        public bool EnableIronVolcano { get; set; }

        [Option("Voidria.VoidriaOptions.OilReservoirTitle", "Voidria.VoidriaOptions.OilReservoirTooltip", "STRINGS.UI.DETAILTABS.SIMPLEINFO.GROUPNAME_GEYSERS")]
        [JsonProperty]
        public bool EnableOilReservoir { get; set; }

#if ENABLE_STORY_TRAITS
        [Option("STRINGS.CODEX.STORY_TRAITS.CRITTER_MANIPULATOR.NAME", "STRINGS.CODEX.STORY_TRAITS.CRITTER_MANIPULATOR.DESCRIPTION", "STRINGS.UI.FRONTEND.COLONYDESTINATIONSCREEN.STORY_TRAITS_HEADER")]
        [JsonProperty]
        public bool StoryCritterManipulator { get; set; }

        [Option("STRINGS.CODEX.STORY_TRAITS.MEGA_BRAIN_TANK.NAME", "STRINGS.CODEX.STORY_TRAITS.MEGA_BRAIN_TANK.DESCRIPTION", "STRINGS.UI.FRONTEND.COLONYDESTINATIONSCREEN.STORY_TRAITS_HEADER")]
        [JsonProperty]
        public bool StoryMegaBrainTank { get; set; }


        [Option("STRINGS.CODEX.STORY_TRAITS.LONELYMINION.NAME", "STRINGS.CODEX.STORY_TRAITS.LONELYMINION.DESCRIPTION", "STRINGS.UI.FRONTEND.COLONYDESTINATIONSCREEN.STORY_TRAITS_HEADER")]
        [JsonProperty]
        public bool StoryLonelyMinion { get; set; }

        [Option("STRINGS.CODEX.STORY_TRAITS.MORB_ROVER_MAKER.NAME", "STRINGS.CODEX.STORY_TRAITS.MORB_ROVER_MAKER.DESCRIPTION", "STRINGS.UI.FRONTEND.COLONYDESTINATIONSCREEN.STORY_TRAITS_HEADER")]
        [JsonProperty]
        public bool StoryMorbRoverMaker { get; set; }

        [Option("STRINGS.CODEX.STORY_TRAITS.FOSSILHUNT.NAME", "STRINGS.CODEX.STORY_TRAITS.FOSSILHUNT.DESCRIPTION", "STRINGS.UI.FRONTEND.COLONYDESTINATIONSCREEN.STORY_TRAITS_HEADER")]
        [JsonProperty]
        public bool StoryFossilHunt { get; set; }
#endif // ENABLE_STORY_TRAITS

        public VoidriaOptions()
        {
            EnableIronVolcano = true;
            EnableOilReservoir = true;

#if ENABLE_STORY_TRAITS
            StoryCritterManipulator = true;
            StoryMegaBrainTank = true;
            StoryLonelyMinion = true;
            StoryMorbRoverMaker = true;
            StoryFossilHunt = true;
#endif // ENABLE_STORY_TRAITS
        }
    }
}
