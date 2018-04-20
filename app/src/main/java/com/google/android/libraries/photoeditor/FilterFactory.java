package com.google.android.libraries.photoeditor;

import com.google.android.libraries.photoeditor.FilterDefs.FilterParameterType;
import com.google.android.libraries.photoeditor.FilterDefs.FilterType;
import com.google.android.libraries.photoeditor.filterparameters.Ambiance2FilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.AutoCorrectFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.AutoEnhanceFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.BlackAndWhiteFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.CenterFocusParameter;
import com.google.android.libraries.photoeditor.filterparameters.CropAndRotateFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.CropFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.DetailsFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.DramaFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.EmptyFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.FilmFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.FilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.FixedFrameFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.FramesFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.GrungeFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.RetroluxFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.StraightenFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.TiltAndShiftFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.TuneImageFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.UPointFilterParameter;
import com.google.android.libraries.photoeditor.filterparameters.UPointParameter;
import com.google.android.libraries.photoeditor.filterparameters.VintageFilterParameter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class FilterFactory {
    private static final String FILTER_NAME_FIELD = "filterName";
    private static final String FILTER_PARAMETERS_FIELD = "filterParams";
    private static final String SUB_FILTERS_FIELD = "subFilters";

    public static FilterParameter createFilterParameter(int filterType) {
        switch (filterType) {
            case 1:
                return EmptyFilterParameter.INSTANCE;
            case 2:
                return new AutoCorrectFilterParameter();
            case 3:
                return new UPointFilterParameter();
            case 4:
                return new TuneImageFilterParameter();
            case 5:
                return new StraightenFilterParameter();
            case 6:
                return new CropFilterParameter();
            case 7:
                return new BlackAndWhiteFilterParameter();
            case 8:
                return new VintageFilterParameter();
            case 9:
                return new DramaFilterParameter();
            case 10:
                return new GrungeFilterParameter();
            case 11:
                return new CenterFocusParameter();
            case 12:
                return new FramesFilterParameter();
            case 13:
                return new DetailsFilterParameter();
            case 14:
                return new TiltAndShiftFilterParameter();
            case 16:
                return new RetroluxFilterParameter();
            case 17:
                return new FixedFrameFilterParameter();
            case 18:
                return new AutoEnhanceFilterParameter();
            case 20:
                return new CropAndRotateFilterParameter();
            case 100:
                return new Ambiance2FilterParameter();
            case 200:
                return new FilmFilterParameter();
            case 300:
                return new UPointParameter();
            default:
                return null;
        }
    }

    public static FilterParameter parseJson(JSONObject filterJson) throws JSONException {
        int i;
        FilterParameter filter = createFilterParameter(FilterType.getFilterId(filterJson.getString(FILTER_NAME_FIELD)));
        if (filterJson.has(FILTER_PARAMETERS_FIELD)) {
            JSONObject paramsJson = filterJson.getJSONObject(FILTER_PARAMETERS_FIELD);
            int[] parameterKeys = filter.getParameterKeys();
            for (i = 0; i < parameterKeys.length; i++) {
                filter.setParameterValueOld(parameterKeys[i], paramsJson.getInt(FilterParameterType.getParameterName(parameterKeys[i])));
            }
        }
        if (filter.getSubParameters() != null && filterJson.has(SUB_FILTERS_FIELD)) {
            JSONArray subFiltersJson = filterJson.getJSONArray(SUB_FILTERS_FIELD);
            for (i = 0; i < subFiltersJson.length(); i++) {
                filter.addSubParameters(parseJson(subFiltersJson.getJSONObject(i)));
            }
        }
        return filter;
    }

    public static FilterParameter parseReader(Reader input) throws IOException, JSONException {
        int ch;
        do {
            ch = input.read();
        } while (Character.isSpaceChar(ch));
        if (ch != 123) {
            throw new IOException("Invalid JSON object start marker.");
        }
        StringBuilder jsonBlock = new StringBuilder(1024);
        jsonBlock.append((char) ch);
        int depth = 1;
        do {
            ch = input.read();
            jsonBlock.append((char) ch);
            switch (ch) {
                case 123:
                    depth++;
                    continue;
                case 125:
                    depth--;
                    continue;
                default:
                    break;
            }
        } while (depth > 0);
        return parseJson(new JSONObject(jsonBlock.toString()));
    }

    public static List<FilterParameter> parseStringArray(String filterArrayString) {
        try {
            JSONArray filterArrayJson = new JSONArray(filterArrayString);
            List<FilterParameter> filterParameters = new ArrayList();
            for (int i = 0; i < filterArrayJson.length(); i++) {
                filterParameters.add(parseJson(filterArrayJson.getJSONObject(i)));
            }
            return filterParameters;
        } catch (JSONException e) {
            return null;
        }
    }

    public static FilterParameter parseString(String filterString) {
        try {
            return parseReader(new StringReader(filterString));
        } catch (Exception e) {
            return null;
        }
    }

    public static void toWriter(FilterParameter filter, Writer output) throws IOException {
        output.append("{\"").append(FILTER_NAME_FIELD).append("\":\"");
        output.append(FilterType.getFilterName(filter.getFilterType()));
        output.append("\"");
        int[] parameterKeys = filter.getParameterKeys();
        if (parameterKeys != null && parameterKeys.length > 0) {
            output.append(",\"").append(FILTER_PARAMETERS_FIELD).append("\":{");
            int i = 0;
            while (i < parameterKeys.length) {
                output.append(i > 0 ? ",\"" : "\"");
                output.append(FilterParameterType.getParameterName(parameterKeys[i]));
                output.append("\":");
                output.append(String.format("%d", new Object[]{Integer.valueOf(filter.getParameterValueOld(parameterKeys[i]))}));
                i++;
            }
            output.append("}");
        }
        List<FilterParameter> subParameters = filter.getSubParameters();
        if (subParameters != null && subParameters.size() > 0) {
            output.append(",\"").append(SUB_FILTERS_FIELD).append("\":[");
            boolean isFirst = true;
            for (FilterParameter subFilter : subParameters) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    output.append(",");
                }
                toWriter(subFilter, output);
            }
            output.append("]");
        }
        output.append("}");
    }

    public static String toString(FilterParameter filter) {
        Writer stringWriter = new StringWriter(1024);
        try {
            toWriter(filter, stringWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static String toString(List<FilterParameter> filterArray) {
        Writer stringWriter = new StringWriter(1024);
        try {
            toWriter((List) filterArray, stringWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void toWriter(List<FilterParameter> filterArray, Writer output) throws IOException {
        output.append("[");
        if (filterArray != null && filterArray.size() > 0) {
            boolean isFirst = true;
            for (FilterParameter filter : filterArray) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    output.append(",");
                }
                toWriter(filter, output);
            }
        }
        output.append("]");
    }
}
