using HarmonyLib;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;

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
        }
    }
}
