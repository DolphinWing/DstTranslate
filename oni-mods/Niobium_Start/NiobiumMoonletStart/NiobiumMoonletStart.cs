using HarmonyLib;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;
using PeterHan.PLib.PatchManager;

namespace DolphinWing.ONI.NiobiumMoonletStart
{
    class NiobiumMoonletStart : KMod.UserMod2
    {
        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            //Debug.Log("NiobiumMoonletStart OnLoad");
            PUtil.InitLibrary();
            new PLocalization().Register();
            //new PPatchManager(harmony).RegisterPatchClass(typeof(NiobiumMoonletStart));
        }

        /// <summary>
		/// Registers the strings used in this mod.
		/// </summary>
		[PLibMethod(RunAt.AfterDbInit)]
        internal static void InitStrings()
        {
            Debug.Log("NiobiumMoonletStart InitStrings");
            Strings.Add("STRINGS.CLUSTER_NAMES.NIOBIUMMOONLETSTART.NAME", Constants.CLUSTER_NAME);
            Strings.Add("STRINGS.CLUSTER_NAMES.NIOBIUMMOONLETSTART.DESCRIPTION", Constants.CLUSTER_DESCRIPTION);
        }
    }
}
