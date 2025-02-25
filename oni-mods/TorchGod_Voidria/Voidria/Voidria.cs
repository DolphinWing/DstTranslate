using HarmonyLib;
using Klei.CustomSettings;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using PeterHan.PLib.Options;
using ProcGen;
using ProcGenGame;
using System.Collections.Generic;
using System.IO;

namespace Voidria
{
    class Voidria : KMod.UserMod2
    {
        public static LocString NAME = (LocString)"Voidria";
        public static LocString DESCRIPTION = (LocString)"Hopeless void. Resources scarced and limited. GEYSERS NOT INCLUDED.\n\n<smallcaps>Duplicants MUST work to DEATH to make the colony thrive again.</smallcaps>";

        public static LocString WARP_NAME = (LocString)"Rocker";
        public static LocString WARP_DESC = (LocString)"A tiny rock needs one small step.";

        public static LocString LAND_NAME = (LocString)"Landing Zone";
        public static LocString LAND_DESC = (LocString)"A tiny rock to land your little rocket.";

        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            PUtil.InitLibrary();
            new PLocalization().Register();
            new POptions().RegisterOptions(this, typeof(VoidriaOptions));

            Strings.Add("STRINGS.CLUSTER_NAMES.VOIDRIA.NAME", NAME);
            Strings.Add("STRINGS.CLUSTER_NAMES.VOIDRIA.DESCRIPTION", DESCRIPTION);
            Strings.Add("STRINGS.SUBWORLDS.VOIDRIA.NAME", NAME);
            Strings.Add("STRINGS.SUBWORLDS.VOIDRIA.DESC", DESCRIPTION);
            Strings.Add("STRINGS.WORLDS.TINYLANDINGZONE.NAME", LAND_NAME);
            Strings.Add("STRINGS.WORLDS.TINYLANDINGZONE.DESCRIPTION", LAND_DESC);
            Strings.Add("STRINGS.WORLDS.TINYWARPSURFACE.NAME", WARP_NAME);
            Strings.Add("STRINGS.WORLDS.TINYWARAPSURFACE.DESCRIPTION", WARP_DESC);
            Strings.Add("STRINGS.WORLDS.VOIDRIA.NAME", NAME);
            Strings.Add("STRINGS.WORLDS.VOIDRIA.DESCRIPTION", DESCRIPTION);
            Strings.Add("STRINGS.WORLDS.VOIDRIASO.NAME", NAME);
            Strings.Add("STRINGS.WORLDS.VOIDRIASO.DESCRIPTION", DESCRIPTION);
            Strings.Add("STRINGS.WORLDS.VOIDRIAMINI.NAME", NAME);
            Strings.Add("STRINGS.WORLDS.VOIDRIAMINI.DESCRIPTION", DESCRIPTION);
        }

        public static bool IsVoaCluster()
        {
            SettingLevel current = CustomGameSettings.Instance.GetCurrentQualitySetting((SettingConfig)CustomGameSettingConfigs.ClusterLayout);
            if (current == null) return false; // unknown cluster

            ClusterLayout clusterData = SettingsCache.clusterLayouts.GetClusterData(current.id);
            string prefix = clusterData.GetCoordinatePrefix();
            return prefix.StartsWith("VOA-TG-"); // B: base game. C: Spaced Out classic. M: Spaced Out style.
        }

        /// <summary>
        /// Applied to MutatedWorldData() to remove all geysers on hard mode on 100 K.
        /// </summary>
        [HarmonyPatch(typeof(MutatedWorldData), MethodType.Constructor, typeof(ProcGen.World),
            typeof(List<WorldTrait>), typeof(List<WorldTrait>))]
        public static class MutatedWorldData_Constructor_Patch
        {
            /// <summary>
            /// Applied after the constructor runs.
            /// </summary>
            internal static void Postfix(MutatedWorldData __instance)
            {
                var world = __instance.world;
                if (world.name.StartsWith("Voidria.Voidria.") == false) return; // no need to check further
                //PUtil.LogDebug("Checking for " + world.name);

                var options = VoidriaOptions.GetInstance();
                //if (options == null) return; // no need to change anything

                var spaced = DlcManager.IsContentSubscribed(DlcManager.EXPANSION1_ID);
                var frosty = DlcManager.IsContentSubscribed(DlcManager.DLC2_ID);
                var bionic = DlcManager.IsContentSubscribed(DlcManager.DLC3_ID);
                //PUtil.LogDebug("DLC own: " + spaced + ", " + frosty + ", " + bionic);

                var dlcMixing = CustomGameSettings.Instance.GetCurrentDlcMixingIds();
                frosty = dlcMixing.Contains(DlcManager.DLC2_ID);
                bionic = dlcMixing.Contains(DlcManager.DLC3_ID);
                //PUtil.LogDebug("DLC mixing: " + spaced + ", " + frosty + ", " + bionic);

                var stories = CustomGameSettings.Instance.GetCurrentStories();
#if DEBUG
                foreach (var story in stories)
                {
                    PUtil.LogDebug("story: " + story);
                }
#endif

#if DEBUG
                var teleporter = CustomGameSettings.Instance.GetCurrentQualitySetting(CustomGameSettingConfigs.Teleporters);
                PUtil.LogDebug("teleporter: " + teleporter.coordinate_value);
#endif
                var rules = world.worldTemplateRules;
                if (rules != null)
                {
                    List<ProcGen.World.TemplateSpawnRules> removed = new List<ProcGen.World.TemplateSpawnRules>();
                    foreach (var rule in rules)
                    {
#if DEBUG
                        PUtil.LogDebug("==>" + rule.ruleId);
                        var names = rule.names;
                        foreach (var n in names)
                        {
                            PUtil.LogDebug("  " + n);
                        }
#endif

                        if (rule.names.Contains("geysers/molten_iron"))
                        {
                            if (options != null && options?.EnableIronVolcano == false)
                            {
                                removed.Add(rule); //world.worldTemplateRules?.Remove(rule);
                                PUtil.LogDebug("... remove iron volcano");
                            }
                        }
                        if (rule.names.Contains("poi/oil/small_oilpockets_geyser_a"))
                        {
                            if (options.EnableOilReservoir == false)
                            {
                                removed.Add(rule); //world.worldTemplateRules?.Remove(rule);
                                PUtil.LogDebug("... remove oil pocket geyser");
                            }
                        }

                        if (rule.ruleId?.StartsWith("tg_Story_") == true)
                        {
                            var ruleId = rule.ruleId.Substring(9);
                            if (stories.Contains(ruleId) == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + ruleId);
                            }
                        }

                        if (rule.ruleId?.StartsWith("tg_Critter_") == true)
                        {
                            if (options.EnableCritters == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + rule.ruleId);
                            }
                            else if (frosty && rule.ruleId?.StartsWith("tg_Critter_Vanilla") == true) 
                            {
                                PUtil.LogDebug("... add frosty critters");
                                rule.names.Add("dlc2::critters/tg_bammoth");
                                rule.names.Add("dlc2::critters/tg_flox");
                                rule.names.Add("dlc2::critters/tg_sugar_bug_seagul");
                            }
                        }

                        if (rule.ruleId?.StartsWith("tg_gift") == true)
                        {
                            if (options.EnableGift == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + rule.ruleId);
                            }
                            else if (frosty && rule.ruleId?.StartsWith("tg_gift_base") == true)
                            {
                                rule.names.Add("dlc2::bases/tg_wood_pile");
                                PUtil.LogDebug("... add Frosty Wood pile");
                            }
                        }

                        if (rule.ruleId?.StartsWith("temporalTear") == true)
                        {
                            if (options.EnableTearOpener == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + rule.ruleId);
                            }
                        }
                    }

                    if (removed.Count > 0) // remove them from list
                        foreach (var rule in removed)
                        {
                            world.worldTemplateRules?.Remove(rule);
                        }
                }
            }
        }

        [HarmonyPatch(typeof(ColonyDestinationSelectScreen), "OnSpawn")]
        public static class ColonyDestinationSelectScreen_OnSpawn_Patch
        {
            public static void Prefix()
            {
                //PUtil.LogDebug("ColonyDestinationSelectScreen_OnSpawn_Patch.Prefix");
            }
        }

        [HarmonyPatch(typeof(ClusterPOIManager), "RegisterTemporalTear")]
        public static class ClusterPOIManager_RegisterTemporalTear_Patch
        {
            public static void Postfix(TemporalTear temporalTear, ClusterPOIManager __instance)
            {
                //PUtil.LogDebug("ClusterPOIManager_RegisterTemporalTear_Patch.Postfix");

                if (IsVoaCluster() == false) return; // don't care about other clusters

                var options = VoidriaOptions.GetInstance();
                if (options.EnableTearOpener) return; // player will do by themselves
                if (temporalTear.IsOpen() == false)
                {
                    temporalTear.Open();
                    PUtil.LogDebug("Open Temporal Tear");
                }
            }
        }
    }
}
