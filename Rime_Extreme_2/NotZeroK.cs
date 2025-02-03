using HarmonyLib;
using Klei.CustomSettings;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using PeterHan.PLib.Options;
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

        public const Temperature.Range TG_AbsoluteZero = (Temperature.Range)30; // not to clash with other mods
        public const Temperature.Range TG_SuperCold = (Temperature.Range)29; // not to clash with other mods

        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            PUtil.InitLibrary();
            new PLocalization().Register();
            new POptions().RegisterOptions(this, typeof(NotZeroOptions));

            // refs: Baator
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

#if ENABLE_INSTANT_MODE
        /// <summary>
        /// Retrieves the "minimum" temperature of an element on stock worlds. However, on
        /// 100 K, returns 1 K to disable the check.
        /// </summary>
        /// <param name="element">The element to look up.</param>
        /// <param name="worldGen">The currently generating world.</param>
        /// <returns>The minimum temperature to be used for world gen.</returns>
        private static float GetMinTemperature(Element element, WorldGen worldGen)
        {
            var world = worldGen?.Settings?.world;
            return (world != null && world.name.StartsWith("NotZeroK.WorldConstants")) ? 1.0f : element.lowTemp;
        }

        // refs: https://github.com/peterhaneve/ONIMods/blob/main/Challenge100K/Challenge100K.cs
        /// <summary>
        /// Applied to TerrainCell to allow elements to be spawned in at lower than their
        /// normal transition temperature (and thus instantly freeze).
        /// </summary>
        [HarmonyPatch(typeof(TerrainCell), "ApplyBackground")]
        public static class TerrainCell_ApplyBackground_Patch
        {
            internal static IEnumerable<CodeInstruction> Transpiler(
                    IEnumerable<CodeInstruction> method)
            {
                var options = POptions.ReadSettings<NotZeroOptions>();
                bool instantMode = false;
                if (options != null && options.InstantMode)
                {
                    instantMode = true;
                    PUtil.LogDebug("Enable instant mode.");
                }

                var target = typeof(Element).GetFieldSafe(nameof(Element.lowTemp), false);
                var replacement = typeof(NotZeroK).GetMethodSafe(nameof(
                    GetMinTemperature), true, typeof(Element), typeof(WorldGen));
                foreach (var instruction in method)
                    if (instantMode && // only enable this from options
                        instruction.opcode == OpCodes.Ldfld && 
                        target != null && target == (FieldInfo)instruction.operand)
                    {
                        // With the Element on the stack, push the WorldGen (first arg)
                        yield return new CodeInstruction(OpCodes.Ldarg_1);
                        // Replacement for "Element.lowTemp"
                        yield return new CodeInstruction(OpCodes.Call, replacement);
                    }
                    else
                        yield return instruction;
            }
        }
#endif // ENABLE_INSTANT_MODE

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

            private static Temperature.Range GetStartingBiomeTemperature(Temperature.Range temp)
            {
                var options = POptions.ReadSettings<NotZeroOptions>();
                NotZeroOptions.MapMode mode = options != null ? options.Mode : NotZeroOptions.MapMode.Balanced;
                if (temp == Temperature.Range.Mild)
                {
                    // starting biome keeps dupes survival
                    switch (mode)
                    {
                        case NotZeroOptions.MapMode.Crazy:
                            return TG_SuperCold;
                        case NotZeroOptions.MapMode.Easy:
                            return temp;
                        default:
                            return Temperature.Range.VeryCold;
                    }
                }
                return mode == NotZeroOptions.MapMode.Crazy ? TG_SuperCold : temp; // Override temp
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
                if (temp >= Temperature.Range.ExtremelyCold && temp <= Temperature.Range.ExtremelyHot)
                {
                    if (worldGen.isStartingWorld || world.name.StartsWith("NotZeroK.WorldConstants."))
                    {
                        __result = GetStartingBiomeTemperature(temp);
                    }
                    else
                    {
                        PUtil.LogDebug("override Temperature.Range." + temp);
                        __result = TG_AbsoluteZero; // Override temp
                    }
                }
            }
        }
    }
}
