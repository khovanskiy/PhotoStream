package ru.example.PhotoStream;

import android.util.Log;

import java.io.Serializable;
import java.util.*;

public class OKParameters extends LinkedHashMap<String, String> implements Serializable {
    private static final long serialVersionUID = -7183150344504033644L;
    public OKParameters() {
        super();
    }

    /**
     * Initializes parameters from another map
     * @param fromMap Map with parameters
     */
    public OKParameters(Map<String, String> fromMap) {
        super(fromMap);
    }

    public static OKParameters from(Object... args) {
        if (args.length % 2 != 0) {
            Log.w("OKUtil", "Params must be paired. Last one is ignored");
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>(args.length / 2);
        for (int i = 0; i + 1 < args.length; i += 2) {
            if (args[i] == null || args[i + 1] == null || !(args[i] instanceof String)) {
                Log.e("OK SDK", "Error while using mapFrom", new Exception("Key and value must be specified. Key must be string"));
                continue;
            }
            result.put((String) args[i], String.valueOf(args[i + 1]));
        }
        return new OKParameters(result);
    }

    public static OKParameters implode(String key, String glue, Collection<?> objects) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(1);
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<?> iterator = objects.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
            if (iterator.hasNext()) {
                stringBuilder.append(glue);
            }
        }
        result.put(key, stringBuilder.toString());
        return new OKParameters(result);
    }
}
