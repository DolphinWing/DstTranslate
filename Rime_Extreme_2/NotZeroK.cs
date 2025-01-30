using HarmonyLib;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using ProcGen;
using ProcGenGame;
using System;
using System.Collections.Generic;

namespace NotZeroK
{
    class NotZeroK : KMod.UserMod2
    {
        private static Dictionary<Temperature.Range, string> temperatureTable = new Dictionary<Temperature.Range, string>();
        private static Dictionary<string, object> temperatureReverseTable = new Dictionary<string, object>();

        private static void AddHashToTable(Temperature.Range hash, string id)
        {
            temperatureTable.Add(hash, id);
            temperatureReverseTable.Add(id, (object)hash);
        }

        /// <summary>
        /// The enum value used for 100K subworlds.
        /// </summary>
        public const Temperature.Range TG_AbsoluteZero = (Temperature.Range)18;
        public const Temperature.Range TG_SuperCold = (Temperature.Range)19;

        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            PUtil.InitLibrary();
            new PLocalization().Register();

            AddHashToTable(TG_AbsoluteZero, "AbsoluteZero");
            AddHashToTable(TG_SuperCold, "SuperCold");
        }

        [HarmonyPatch(typeof(Enum), "ToString", new Type[] { })]
        public static class Temperatures_ToString_Patch
        {
            public static bool Prefix(ref Enum __instance, ref string __result) => 
                !(__instance is Temperature.Range) || 
                !temperatureTable.TryGetValue((Temperature.Range)__instance, out __result);
        }

        [HarmonyPatch(typeof(Enum), "Parse", new Type[] { typeof(Type), typeof(string), typeof(bool) })]
        public static class Temperatures_Parse_Patch
        {
            public static bool Prefix(Type enumType, string value, ref object __result) => 
                !enumType.Equals(typeof(Temperature.Range)) || 
                !temperatureReverseTable.TryGetValue(value, out __result);
        }

        ///// <summary>
        ///// Applied to TerrainCell to "fix" the temperature range of Volcanoes, Magma Channels,
        ///// Buried Oil, Subsurface Ocean, and Irregular Oil to 100 K.
        ///// </summary>
        //[HarmonyPatch(typeof(TerrainCell), "GetTemperatureRange", typeof(WorldGen))]
        //public static class TerrainCell_GetTemperatureRange_Patch
        //{
        //    /// <summary>
        //    /// Applied after GetTemperatureRange runs.
        //    /// </summary>
        //    internal static void Postfix(WorldGen worldGen, ref Temperature.Range __result)
        //    {
        //        var world = worldGen.Settings?.world;
        //        var temp = __result;
        //        if (world != null && world.name.StartsWith("NotZeroK.WorldConstants.") && 
        //            temp > Temperature.Range.VeryCold && temp <= Temperature.Range.ExtremelyHot)
        //        {
        //            __result = TG_AbsoluteZero; // Override temp
        //        }
        //    }
        //}
    }
}
