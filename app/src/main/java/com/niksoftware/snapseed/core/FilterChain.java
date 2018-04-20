package com.niksoftware.snapseed.core;

import android.util.SparseArray;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.json.JSONArray;
import org.json.JSONException;

public class FilterChain {
    private final SparseArray<FilterChainNode> filterNodes = new SparseArray();

    public void clear() {
        this.filterNodes.clear();
    }

    public void addFilterParameter(FilterParameter filterParameter) {
        this.filterNodes.append(this.filterNodes.size(), new FilterChainNode(filterParameter));
    }

    public int size() {
        return this.filterNodes.size();
    }

    public SparseArray<FilterChainNode> getFilterNodes() {
        return this.filterNodes;
    }

    public void removeLastFilterNode() {
        if (this.filterNodes.size() > 0) {
            this.filterNodes.removeAt(this.filterNodes.size() - 1);
        }
    }

    public String toStringJson() {
        StringWriter stringWriter = new StringWriter(1024);
        try {
            toWriterJson(stringWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private void toWriterJson(Writer output) throws IOException {
        output.append("[");
        for (int i = 0; i < this.filterNodes.size(); i++) {
            if (i != 0) {
                output.append(",");
                FilterFactory.toWriter(((FilterChainNode) this.filterNodes.get(i)).getFilterParameter(), output);
            }
        }
        output.append("]");
    }

    public static FilterChain parseFilterChainString(String filterChainString) {
        try {
            JSONArray filterChainJson = new JSONArray(filterChainString);
            FilterChain filterChain = new FilterChain();
            for (int i = 0; i < filterChainJson.length(); i++) {
                filterChain.addFilterParameter(FilterFactory.parseJson(filterChainJson.getJSONObject(i)));
            }
            return filterChain;
        } catch (JSONException e) {
            return null;
        }
    }
}
