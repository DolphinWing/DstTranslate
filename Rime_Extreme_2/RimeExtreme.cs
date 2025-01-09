using HarmonyLib;
using PeterHan.PLib.Core;
using PeterHan.PLib.Database;

namespace RimeExtreme
{
    class RimeExtreme : KMod.UserMod2
    {
        public override void OnLoad(Harmony harmony)
        {
            base.OnLoad(harmony);
            PUtil.InitLibrary();
            new PLocalization().Register();
        }
    }
}
