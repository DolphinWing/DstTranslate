using HarmonyLib;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using ProcGen;
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

        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            PUtil.InitLibrary();
            new PLocalization().Register();

            AddHashToTable((Temperature.Range)18, "AbsoluteZero");
            AddHashToTable((Temperature.Range)19, "SuperCold");
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
    }
}
