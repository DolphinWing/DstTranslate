using HarmonyLib;

namespace Voidria
{
    public class Patches
    {
        [HarmonyPatch(typeof(Db))]
        [HarmonyPatch("Initialize")]
        public class Db_Initialize_Patch
        {
            public static void Prefix()
            {
                //Debug.Log("I execute before Db.Initialize!");

                //Strings.Add("STRINGS.WORLDS.VOIDRIA.NAME", (string)Voidria.NAME);
                //Strings.Add("STRINGS.WORLDS.VOIDRIA.DESCRIPTION", (string)Voidria.DESCRIPTION);
            }

            public static void Postfix()
            {
                //Debug.Log("I execute after Db.Initialize!");
            }
        }
    }
}
