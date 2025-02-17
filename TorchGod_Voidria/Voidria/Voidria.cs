using HarmonyLib;
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

        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            PUtil.InitLibrary();
            new PLocalization().Register();
            new POptions().RegisterOptions(this, typeof(VoidriaOptions));
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
                var options = POptions.ReadSettings<VoidriaOptions>();
                if (options == null) return; // no need to change anything

                var world = __instance.world;
                if (world.name != "Voidria.Voidria.NAME") return; // no need to check further
                PUtil.LogDebug("Checking for " + world.name);

                var spaced = DlcManager.IsContentSubscribed(DlcManager.EXPANSION1_ID);
                var frosty = DlcManager.IsContentSubscribed(DlcManager.DLC2_ID);
                var bionic = DlcManager.IsContentSubscribed(DlcManager.DLC3_ID);
                PUtil.LogDebug("DLC own: " + spaced + ", " + frosty + ", " + bionic);

                var dlcMixing = CustomGameSettings.Instance.GetCurrentDlcMixingIds();
                frosty = dlcMixing.Contains(DlcManager.DLC2_ID);
                bionic = dlcMixing.Contains(DlcManager.DLC3_ID);
                PUtil.LogDebug("DLC mixing: " + spaced + ", " + frosty + ", " + bionic);

#if ENABLE_STORY_TRAIT_OPTIONS
                var stories = new Dictionary<string, bool>
                {
                    { "CritterManipulator", options.StoryCritterManipulator },
                    { "MegaBrainTank", options.StoryMegaBrainTank },
                    { "LonelyMinion", options.StoryLonelyMinion },
                    { "MorbRoverMaker", options.StoryMorbRoverMaker },
                    { "FossilHunt", options.StoryFossilHunt }
                };
#else
                var stories = CustomGameSettings.Instance.GetCurrentStories();
                foreach (var story in stories)
                {
                    PUtil.LogDebug("story: " + story);
                }
#endif // ENABLE_STORY_TRAIT_OPTIONS

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

                        if (!options.EnableIronVolcano && rule.names.Contains("geysers/molten_iron"))
                        {
                            removed.Add(rule); //world.worldTemplateRules?.Remove(rule);
                            PUtil.LogDebug("... remove iron volcano");
                        }
                        if (!options.EnableOilReservoir && rule.names.Contains("poi/oil/small_oilpockets_geyser_a"))
                        {
                            removed.Add(rule); //world.worldTemplateRules?.Remove(rule);
                            PUtil.LogDebug("... remove oil pocket geyser");
                        }

#if ENABLE_STORY_TRAIT_OPTIONS
                        if (rule.ruleId?.StartsWith("tg_Story_") == true)
                        {
                            var ruleId = rule.ruleId.Substring(9);
                            if (stories.ContainsKey(ruleId) == false || stories[ruleId] == false)
                            {
                                removed.Add(rule); // remove it since disabled or not existed
                                PUtil.LogDebug("... remove " + ruleId);
                            }
                        }
#else
                        if (rule.ruleId?.StartsWith("tg_Story_") == true)
                        {
                            var ruleId = rule.ruleId.Substring(9);
                            if (stories.Contains(ruleId) == false)
                            {
                                removed.Add(rule);
                                PUtil.LogDebug("... remove " + ruleId);
                            }
                        }
#endif // ENABLE_STORY_TRAIT_OPTIONS

                        if (frosty == false && rule.ruleId?.StartsWith("tg_Critter_Frosty") == true)
                        {
                            removed.Add(rule);
                            PUtil.LogDebug("... remove Frosty Planet Pack critters and plants POI");
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
