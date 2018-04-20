package com.niksoftware.snapseed.core;

import android.content.Context;

import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.util.ExtSparseArray;

public final class FilterDefs {

    public static class AspectRatio {
        public static final int Count = 9;
        public static final int Din = 3;
        public static final int Free = 0;
        public static final int Original = 1;
        public static final int a16_9 = 8;
        public static final int a1_1 = 2;
        public static final int a3_2 = 4;
        public static final int a4_3 = 5;
        public static final int a5_4 = 6;
        public static final int a7_5 = 7;
    }

    public static class BackgroundTextureIDs {
        public static final int Grunge0 = 101;
        public static final int Grunge1 = 102;
        public static final int Grunge2 = 103;
        public static final int Grunge3 = 104;
        public static final int Grunge4 = 105;
        public static final int GrungeCount = 5;
        public static final int Vintage0 = 0;
        public static final int Vintage1 = 1;
        public static final int Vintage2 = 2;
        public static final int Vintage3 = 3;
        public static final int VintageCount = 4;
    }

    public static class BackgroundTextureOptions {
        public static final int FlipHorizontally = 1;
        public static final int FlipVertically = 2;
        public static final int None = 0;
        public static final int NotInitialized = 1000;
        public static final int RotateLeft = 3;
        public static final int RotateRight = 4;
    }

    public static class ContextAction {
        public static final int ActivatePreset = 6;
        public static final int InitializeTextureOption = 7;
        public static final int NotInitialized = 1000;
        public static final int Random = 0;
        public static final int RandomLightLeak = 4;
        public static final int RandomScratch = 5;
        public static final int RandomStyle = 2;
        public static final int RandomTexture = 1;
        private static final String[] names = new String[]{"Random", "RandomTexture", "RandomStyle", "RandomLightLeak", "RandomScratch", "ActivatePreset", "InitializeTextureOption"};

        public static String getNameFor(int value) {
            if (value >= 0 && value < names.length) {
                return names[value];
            }
            throw new IllegalArgumentException();
        }
    }

    public static class FilterParameterType {
        public static final int Ambiance = 10;
        public static final int Ambiance2Alpha = 650;
        public static final int Ambiance2AttenMapMaxLODBounds = 655;
        public static final int Ambiance2AttenMapMinLODBounds = 654;
        public static final int Ambiance2Beta = 651;
        public static final int Ambiance2BlackPoint = 652;
        public static final int Ambiance2Denoise = 656;
        public static final int Ambiance2WhitePoint = 653;
        public static final int BlurRadius = 13;
        public static final int BlurStrength = 19;
        public static final int Brightness = 0;
        public static final int Center = 5;
        public static final int CenterSize = 4;
        public static final int CenterX = 24;
        public static final int CenterY = 25;
        public static final int ColorStyle = 241;
        public static final int Contrast = 1;
        public static final int CropAspectRatio = 42;
        public static final int CropCoordinates = 41;
        public static final int CropRotate = 40;
        public static final int FilterStrength = 12;
        public static final int FineTuneColor = 8;
        public static final int FixExposure = 7;
        public static final int FrameFormat = 224;
        public static final int FrameOffset = 221;
        public static final int FrameType = 223;
        public static final int FrameWidth = 222;
        public static final int Generic1 = 201;
        public static final int Generic2 = 202;
        public static final int Generic3 = 203;
        public static final int Generic4 = 204;
        public static final int Grain = 14;
        public static final int Highlights = 21;
        public static final int InnerBrightness = 22;
        public static final int LightLeakId = 234;
        public static final int LightLeakStrength = 232;
        public static final int None = 1000;
        public static final int OuterBrightness = 23;
        private static final int Reserved = 999;
        public static final int Rotation = 18;
        private static ExtSparseArray<String> SERIALIZABLE_PARAMETER_NAME = new ExtSparseArray(64);
        public static final int Saturation = 2;
        public static final int SaturationVariance = 231;
        public static final int ScratchId = 235;
        public static final int ScratchesStrength = 233;
        public static final int Shadows = 20;
        public static final int Sharpening = 15;
        public static final int StraightenAngle360 = 39;
        public static final int StraightenAngleLimited = 38;
        public static final int Structure = 16;
        public static final int Style = 3;
        public static final int StyleStrength = 9;
        public static final int Texture = 101;
        public static final int TextureOffset = 103;
        public static final int TextureOffset2 = 113;
        public static final int TextureOffsetX = 106;
        public static final int TextureOffsetY = 107;
        public static final int TextureOption = 102;
        public static final int TextureOption2 = 105;
        public static final int TextureStrength = 104;
        public static final int Transition = 17;
        public static final int Unknown = -1;
        public static final int VignetteStrength = 6;
        public static final int VignetteTexture1 = 211;
        public static final int VignetteTexture2 = 212;
        public static final int Warmth = 11;
        public static final int X = 501;
        public static final int X_1 = 43;
        public static final int X_2 = 44;
        public static final int Y = 502;
        public static final int Y_1 = 45;
        public static final int Y_2 = 46;

        static {
            SERIALIZABLE_PARAMETER_NAME.append(0, "Brightness");
            SERIALIZABLE_PARAMETER_NAME.append(1, "Contrast");
            SERIALIZABLE_PARAMETER_NAME.append(2, "Saturation");
            SERIALIZABLE_PARAMETER_NAME.append(3, "Style");
            SERIALIZABLE_PARAMETER_NAME.append(4, "CenterSize");
            SERIALIZABLE_PARAMETER_NAME.append(5, "Center");
            SERIALIZABLE_PARAMETER_NAME.append(6, "VignetteStrength");
            SERIALIZABLE_PARAMETER_NAME.append(7, "FixExposure");
            SERIALIZABLE_PARAMETER_NAME.append(8, "FineTuneColor");
            SERIALIZABLE_PARAMETER_NAME.append(9, "StyleStrength");
            SERIALIZABLE_PARAMETER_NAME.append(10, "Ambiance");
            SERIALIZABLE_PARAMETER_NAME.append(11, "Warmth");
            SERIALIZABLE_PARAMETER_NAME.append(12, "FilterStrength");
            SERIALIZABLE_PARAMETER_NAME.append(13, "BlurRadius");
            SERIALIZABLE_PARAMETER_NAME.append(14, "Grain");
            SERIALIZABLE_PARAMETER_NAME.append(15, "Sharpening");
            SERIALIZABLE_PARAMETER_NAME.append(16, "Structure");
            SERIALIZABLE_PARAMETER_NAME.append(17, "Transition");
            SERIALIZABLE_PARAMETER_NAME.append(18, "Rotation");
            SERIALIZABLE_PARAMETER_NAME.append(19, "BlurStrength");
            SERIALIZABLE_PARAMETER_NAME.append(20, "Shadows");
            SERIALIZABLE_PARAMETER_NAME.append(21, "Highlights");
            SERIALIZABLE_PARAMETER_NAME.append(22, "InnerBrightness");
            SERIALIZABLE_PARAMETER_NAME.append(23, "OuterBrightness");
            SERIALIZABLE_PARAMETER_NAME.append(38, "StraightenAngleLimited");
            SERIALIZABLE_PARAMETER_NAME.append(39, "StraightenAngle360");
            SERIALIZABLE_PARAMETER_NAME.append(40, "CropRotate");
            SERIALIZABLE_PARAMETER_NAME.append(41, "CropCoordinates");
            SERIALIZABLE_PARAMETER_NAME.append(42, "CropAspectRatio");
            SERIALIZABLE_PARAMETER_NAME.append(43, "X1");
            SERIALIZABLE_PARAMETER_NAME.append(44, "X2");
            SERIALIZABLE_PARAMETER_NAME.append(45, "Y1");
            SERIALIZABLE_PARAMETER_NAME.append(46, "Y2");
            SERIALIZABLE_PARAMETER_NAME.append(101, "Texture");
            SERIALIZABLE_PARAMETER_NAME.append(102, "TextureOption");
            SERIALIZABLE_PARAMETER_NAME.append(103, "TextureOffset");
            SERIALIZABLE_PARAMETER_NAME.append(104, "TextureStrength");
            SERIALIZABLE_PARAMETER_NAME.append(105, "TextureOption2");
            SERIALIZABLE_PARAMETER_NAME.append(106, "TextureOffsetX");
            SERIALIZABLE_PARAMETER_NAME.append(107, "TextureOffsetY");
            SERIALIZABLE_PARAMETER_NAME.append(113, "TextureOffset2");
            SERIALIZABLE_PARAMETER_NAME.append(201, "Generic1");
            SERIALIZABLE_PARAMETER_NAME.append(202, "Generic2");
            SERIALIZABLE_PARAMETER_NAME.append(203, "Generic3");
            SERIALIZABLE_PARAMETER_NAME.append(204, "Generic4");
            SERIALIZABLE_PARAMETER_NAME.append(211, "VignetteTexture1");
            SERIALIZABLE_PARAMETER_NAME.append(212, "VignetteTexture2");
            SERIALIZABLE_PARAMETER_NAME.append(221, "FrameOffset");
            SERIALIZABLE_PARAMETER_NAME.append(222, "FrameWidth");
            SERIALIZABLE_PARAMETER_NAME.append(223, "FrameType");
            SERIALIZABLE_PARAMETER_NAME.append(224, "FrameFormat");
            SERIALIZABLE_PARAMETER_NAME.append(231, "SaturationVariance");
            SERIALIZABLE_PARAMETER_NAME.append(232, "LightLeakStrength");
            SERIALIZABLE_PARAMETER_NAME.append(233, "ScratchesStrength");
            SERIALIZABLE_PARAMETER_NAME.append(241, "ColorStyle");
            SERIALIZABLE_PARAMETER_NAME.append(501, "X");
            SERIALIZABLE_PARAMETER_NAME.append(502, "Y");
            SERIALIZABLE_PARAMETER_NAME.append(650, "Alpha");
            SERIALIZABLE_PARAMETER_NAME.append(651, "Beta");
            SERIALIZABLE_PARAMETER_NAME.append(652, "BlackPoint");
            SERIALIZABLE_PARAMETER_NAME.append(653, "WhitePoint");
            SERIALIZABLE_PARAMETER_NAME.append(654, "AttenMapMinLODBounds");
            SERIALIZABLE_PARAMETER_NAME.append(655, "AttenMapMaxLODBounds");
            SERIALIZABLE_PARAMETER_NAME.append(656, "Denoise");
            SERIALIZABLE_PARAMETER_NAME.append(1000, "None");
        }

        public static String getParameterName(int parameterId) {
            return (String) SERIALIZABLE_PARAMETER_NAME.get(parameterId, "unknown");
        }

        public static int getParameterId(String parameterName) {
            int index = SERIALIZABLE_PARAMETER_NAME.indexOfValue(parameterName);
            return index >= 0 ? SERIALIZABLE_PARAMETER_NAME.keyAt(index) : -1;
        }

        public static String getParameterTitle(Context context, int parameter) {
            switch (parameter) {
                case 0:
                    return context.getString(R.string.param_Brightness);
                case 1:
                    return context.getString(R.string.param_Contrast);
                case 2:
                    return context.getString(R.string.param_Saturation);
                case 3:
                    return context.getString(R.string.param_Style);
                case 4:
                    return context.getString(R.string.param_CenterSize);
                case 5:
                    return context.getString(R.string.param_Center);
                case 6:
                    return context.getString(R.string.param_VignetteStrength);
                case 7:
                    return context.getString(R.string.param_FixExposure);
                case 8:
                    return context.getString(R.string.param_FineTuneColor);
                case 9:
                    return context.getString(R.string.param_StyleStrength);
                case 10:
                    return context.getString(R.string.param_Ambiance);
                case 11:
                    return context.getString(R.string.param_Warmth);
                case 12:
                    return context.getString(R.string.param_FilterStrength);
                case 13:
                    return context.getString(R.string.param_BlurRadius);
                case 14:
                    return context.getString(R.string.param_Grain);
                case 15:
                    return context.getString(R.string.param_Sharpening);
                case 16:
                    return context.getString(R.string.param_Structure);
                case 17:
                    return context.getString(R.string.param_Transition);
                case 19:
                    return context.getString(R.string.param_BlurStrength);
                case 20:
                    return context.getString(R.string.param_Shadows);
                case 22:
                    return context.getString(R.string.inner_brightness);
                case 23:
                    return context.getString(R.string.outer_brightness);
                case 38:
                    return context.getString(R.string.param_StraightenAngle);
                case 40:
                    return context.getString(R.string.param_CropRotate);
                case 41:
                    return context.getString(R.string.param_CropCoordinates);
                case 42:
                    return context.getString(R.string.param_CropAspectRatio);
                case 104:
                    return context.getString(R.string.param_TextureStrength);
                case 221:
                    return context.getString(R.string.param_FrameOffset);
                case 222:
                    return context.getString(R.string.param_FrameWidth);
                case 231:
                    return context.getString(R.string.param_SaturationVariance);
                case 232:
                    return context.getString(R.string.param_LightLeakStrength);
                case 233:
                    return context.getString(R.string.param_ScratchesStrength);
                case 650:
                    return "[ALPHA]";
                case 651:
                    return "[BETA]";
                case 652:
                    return "[BLACKS]";
                case 653:
                    return "[WHITES]";
                case 656:
                    return context.getString(R.string.param_smoothen);
                case 1000:
                    return "";
                default:
                    return FilterDefs.dummyParameterTitle();
            }
        }
    }

    public static class FilterType {
        public static final int Ambiance2 = 100;
        public static final int AutoCorrect = 2;
        public static final int AutoEnhance = 18;
        public static final int BlackAndWhite = 7;
        public static final int CenterFocus = 11;
        public static final int ComingSoon = -1000;
        public static final int Crop = 6;
        public static final int CropAndRotate = 20;
        public static final int Details = 13;
        public static final int Drama = 9;
        public static final int Empty = 1;
        public static final int Film = 200;
        public static final int FixedFrames = 17;
        public static final int Frames = 12;
        public static final int Grunge = 10;
        public static final int HDR = 15;
        public static final int NoFilter = 1000;
        private static final int Reserved1 = 998;
        private static final int Reserved2 = 999;
        public static final int RetroLux = 16;
        private static ExtSparseArray<String> SERIALIZABLE_FILTER_NAME = new ExtSparseArray(24);
        public static final int StraightenRotate = 5;
        public static final int TiltAndShift = 14;
        public static final int TuneImage = 4;
        public static final int UPoint = 3;
        public static final int UPointItem = 300;
        public static final int Unknown = -1;
        public static final int Vintage = 8;

        public static boolean isCPUFilter(int type) {
            return type == 6 || type == 5;
        }

        static {
            SERIALIZABLE_FILTER_NAME.append(2, "AutoCorrect");
            SERIALIZABLE_FILTER_NAME.append(18, "AutoEnhance");
            SERIALIZABLE_FILTER_NAME.append(3, "UPoint");
            SERIALIZABLE_FILTER_NAME.append(300, "UPointItem");
            SERIALIZABLE_FILTER_NAME.append(4, "TuneImage");
            SERIALIZABLE_FILTER_NAME.append(5, "StraightenRotate");
            SERIALIZABLE_FILTER_NAME.append(6, "Crop");
            SERIALIZABLE_FILTER_NAME.append(7, "BlackAndWhite");
            SERIALIZABLE_FILTER_NAME.append(8, "Vintage");
            SERIALIZABLE_FILTER_NAME.append(9, "Drama");
            SERIALIZABLE_FILTER_NAME.append(10, "Grunge");
            SERIALIZABLE_FILTER_NAME.append(11, "CenterFocus");
            SERIALIZABLE_FILTER_NAME.append(13, "Details");
            SERIALIZABLE_FILTER_NAME.append(14, "TiltAndShift");
            SERIALIZABLE_FILTER_NAME.append(16, "RetroLux");
            SERIALIZABLE_FILTER_NAME.append(17, "FixedFrames");
            SERIALIZABLE_FILTER_NAME.append(200, "Film");
            SERIALIZABLE_FILTER_NAME.append(100, "Ambiance 2");
        }

        public static String getFilterName(int filterId) {
            return (String) SERIALIZABLE_FILTER_NAME.get(filterId, "unknown");
        }

        public static int getFilterId(String filterName) {
            int index = SERIALIZABLE_FILTER_NAME.indexOfValue(filterName);
            return index >= 0 ? SERIALIZABLE_FILTER_NAME.keyAt(index) : -1;
        }
    }

    public static class FrameFormat {
        public static final int Original = 0;
        public static final int Square = 1;
    }

    public static class FrameTextureOption {
        public static final int FlipH = 1;
        public static final int FlipHV = 3;
        public static final int FlipV = 2;
        public static final int None = 0;
    }

    public static class RenderScaleMode {
        public static final int Large = 3;
        public static final int Medium = 2;
        public static final int Small = 1;
        public static final int Undefined = 0;
    }

    public static String dummyParameterTitle() {
        return "NIX";
    }
}
