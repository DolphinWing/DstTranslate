using HarmonyLib;

namespace DolphinWing.ONI.NiobiumMoonletStart
{
    public class Patches
    {
        [HarmonyPatch(typeof(Db))]
        [HarmonyPatch("Initialize")]
        public class Db_Initialize_Patch
        {
            public static void Prefix()
            {
                //Debug.Log("NiobiumMoonletStart: I execute before Db.Initialize!");
            }

            public static void Postfix()
            {
                //Debug.Log("NiobiumMoonletStart: I execute after Db.Initialize!");
            }
        }
    }
}
