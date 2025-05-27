using System;
using System.IO;
using FontLoader.Config;
using TMPro;
using UnityEngine;

namespace FontLoader.Utils
{
    public static class FontUtil
    {
        public static TMP_FontAsset LoadFontAsset(FontConfig config)
        {
            TMP_FontAsset font = null;
            AssetBundle ab = null;
            try
            {
                var platform = Application.platform == RuntimePlatform.WindowsPlayer ? "win": "other";
                var assetPath = Path.Combine(ConfigManager.Instance.configPath, "Assets", platform, config.Filename);
                Debug.Log("[FontLoader] " + platform + " " + assetPath);
                ab = AssetBundle.LoadFromFile(assetPath);

                if (ab == null) {
                    Debug.LogWarning("[FontLoader] Unable to load font asset.");
                    return null;
                }

                var assets = ab.LoadAllAssets<TMP_FontAsset>();
                if (assets.Length <= 0) {
                    Debug.LogWarning("[FontLoader] Unable to load all assets.");
                    return null;
                }

                font = assets[0];
                font.fontInfo.Scale = config.Scale;

                if (Application.platform == RuntimePlatform.LinuxPlayer) {
                    font.material.shader = Resources.Load<TMP_FontAsset>("RobotoCondensed-Regular").material.shader;
                }
            }
            catch (Exception e)
            {
                Debug.LogError($"[FontLoader] {e.Message}");
            }

            AssetBundle.UnloadAllAssetBundles(false);
            return font;
        }
    }
}
