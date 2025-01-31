using HarmonyLib;
using Klei.CustomSettings;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using ProcGen;
using ProcGenGame;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

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
        public const Temperature.Range TG_AbsoluteZero = (Temperature.Range)30;
        public const Temperature.Range TG_SuperCold = (Temperature.Range)29;

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

        // refs: https://github.com/peterhaneve/ONIMods/blob/main/Challenge100K/Challenge100K.cs
        /// <summary>
        /// Applied to TerrainCell to "fix" the temperature range of everything.
        /// </summary>
        [HarmonyPatch(typeof(TerrainCell), "GetTemperatureRange", typeof(WorldGen))]
        public static class TerrainCell_GetTemperatureRange_Patch
        {
            private static bool IsAbzModded(string prefix)
            {
                if (prefix.StartsWith("ABZ-TG")) return true;
                if (prefix.StartsWith("M-ABZ-TG")) return true;
                return false;
            }

            /// <summary>
            /// Applied after GetTemperatureRange runs.
            /// </summary>
            internal static void Postfix(WorldGen worldGen, ref Temperature.Range __result)
            {
                if (worldGen.Settings == null) return; // no need to check it

                SettingLevel current = CustomGameSettings.Instance.GetCurrentQualitySetting((SettingConfig)CustomGameSettingConfigs.ClusterLayout);
                if (current == null) return; // unknown cluster

                ClusterLayout clusterData = SettingsCache.clusterLayouts.GetClusterData(current.id);
                //Debug.Log("Not0K " + clusterData.GetCoordinatePrefix());
                string prefix = clusterData.GetCoordinatePrefix();
                if (IsAbzModded(prefix) == false) return; // we only cares about ABZ

                var world = worldGen.Settings?.world;
                if (world == null) return; // unknown world

                var temp = __result;
                if (temp > Temperature.Range.ExtremelyCold && temp <= Temperature.Range.ExtremelyHot)
                {
                    if (worldGen.isStartingWorld && temp == Temperature.Range.Mild) {
                        // ignore it because we don't want dupes die too quickly
                    } else
                    {
                        Debug.Log("Not0K override " + temp);
                        __result = TG_AbsoluteZero; // Override temp
                    }
                }
            }
        }
    }
}
