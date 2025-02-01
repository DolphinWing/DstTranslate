using Newtonsoft.Json;
using PeterHan.PLib.Options;

namespace NotZeroK
{
    [JsonObject(MemberSerialization.OptIn)]
    [ModInfo("https://github.com/DolphinWing/DstTranslate/tree/master/workshop-3413401611")]
    class NotZeroOptions
    {
#if ENABLE_INSTANT_MODE
        [Option("Instant Mode", "You will run out of time!")]
        [JsonProperty]
        public bool InstantMode { get; set; }
#endif // ENABLE_INSTANT_MODE

        [Option("NotZeroK.WorldConstants.ENABLE_HARD_MODE", "NotZeroK.WorldConstants.ENABLE_HARD_MODE_DESC")]
        [JsonProperty]
        public bool HardMode { get; set; }

        public NotZeroOptions()
        {
#if ENABLE_INSTANT_MODE
            InstantMode = false; // TerrainCell_ApplyBackground_Patch
#endif // ENABLE_INSTANT_MODE
            HardMode = false; // TerrainCell_GetTemperatureRange_Patch
        }
    }
}
