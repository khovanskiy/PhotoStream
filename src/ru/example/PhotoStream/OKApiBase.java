package ru.example.PhotoStream;

/**
 * Created by victor on 22.02.2015.
 */
public class OKApiBase {
    OKRequest prepareRequest(String methodName, OKParameters methodParameters,
                             OKParser responseParser) {
        OKRequest result = new OKRequest(methodName, methodParameters);
        result.setResponseParser(responseParser);
        return result;
    }
}
