﻿using HarmonyLib;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using PeterHan.PLib.Options;
using ProcGen;
using System.Collections.Generic;

namespace Voidria
{
    class Voidria : KMod.UserMod2
    {
        public static LocString NAME = (LocString)"Voidria";
        public static LocString DESCRIPTION = (LocString)"Hopeless void. Resources scarced and limited. GEYSERS NOT INCLUDED.\n\nDuplicants MUST work to DEATH to make the colony thrive again.";

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

        //[HarmonyPatch(typeof(ColonyDestinationSelectScreen), "OnSpawn")]
        //public static class ColonyDestinationSelectScreen_OnSpawn_Patch
        //{
        //    public static void Prefix()
        //    {
        //        PUtil.LogDebug("ColonyDestinationSelectScreen_OnSpawn_Patch Prefix");
        //    }
        //}

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
                PUtil.LogDebug("Checking for " + world.name);

                var options = POptions.ReadSettings<VoidriaOptions>();
                //if (options == null) return; // no need to change anything

                var spaced = DlcManager.IsContentSubscribed(DlcManager.EXPANSION1_ID);
                var frosty = DlcManager.IsContentSubscribed(DlcManager.DLC2_ID);
                var bionic = DlcManager.IsContentSubscribed(DlcManager.DLC3_ID);
                PUtil.LogDebug("DLC own: " + spaced + ", " + frosty + ", " + bionic);

                var dlcMixing = CustomGameSettings.Instance.GetCurrentDlcMixingIds();
                frosty = dlcMixing.Contains(DlcManager.DLC2_ID);
                bionic = dlcMixing.Contains(DlcManager.DLC3_ID);
                PUtil.LogDebug("DLC mixing: " + spaced + ", " + frosty + ", " + bionic);

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
                            if (options != null && options?.EnableOilReservoir == false)
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
                            if (options != null && options.EnableCritters == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + rule.ruleId);
                            }
                            else if (rule.ruleId?.StartsWith("tg_Critter_Vanilla") == true && frosty) 
                            {
                                PUtil.LogDebug("... add frosty critters");
                                rule.names.Add("dlc2::critters/tg_bammoth");
                                rule.names.Add("dlc2::critters/tg_flox");
                                rule.names.Add("dlc2::critters/tg_sugar_bug_seagul");
                            }
                        }

                        if (rule.ruleId?.StartsWith("tg_gift") == true)
                        {
                            if (options != null && options.EnableGift == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + rule.ruleId);
                            }
                            else if(rule.ruleId?.StartsWith("tg_gift_basic") == true && frosty)
                            {
                                rule.names.Add("dlc2::bases/tg_wood_pile");
                                PUtil.LogDebug("... add Frosty Wood pile");
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
    }
}
