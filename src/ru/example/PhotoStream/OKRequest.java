package ru.example.PhotoStream;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import ru.example.PhotoStream.Activities.UIActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OKRequest {

    private static final Executor mBackgroundExecutor = Executors.newCachedThreadPool();

    /**
     * Selected method name
     */
    public final String methodName;
    /**
     * Passed parameters for method
     */
    private final OKParameters methodParameters;
    /**
     * Response parser
     */
    private OKParser mModelParser;
    /**
     * Looper which starts request
     */
    private Looper mLooper;
    /**
     * Specify listener for current request
     */
    protected OKRequestListener requestListener;
    /**
     * Set to false if you don't need automatic model parsing
     */
    public boolean shouldParseModel;

    public OKRequest(String method) {
        this(method, null);
    }

    public OKRequest(String method, OKParameters parameters) {
        this.methodName = method;
        this.methodParameters = parameters;
    }

    public void executeWithListener(OKRequestListener requestListerner) {
        this.requestListener = requestListerner;
        execute();
    }

    public void setRequestListener(OKRequestListener listener) {
        this.requestListener = listener;
    }

    public void execute() {
        mLooper = Looper.myLooper();
        mBackgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    public void setResponseParser(OKParser parser) {
        mModelParser = parser;
        if (mModelParser != null) {
            shouldParseModel = true;
        }
    }

    protected void start() {
        try {
            String response = UIActivity.getAPI().request(methodName, methodParameters, "get");
            JSONObject jsonResponse = new JSONObject(response);
            Object parsedModel = null;
            if (shouldParseModel) {
                parsedModel = mModelParser.createModel(jsonResponse);
            }
            provideResponse(parsedModel);
        } catch (Exception e) {
            provideError(new OKError(e.getMessage()));
        }
    }

    /**
     * Method used for response processing
     *
     * @param parsedModel  model parsed from json
     */
    protected void provideResponse(final Object parsedModel) {
        final OKResponse response = new OKResponse();
        response.request = this;
        response.parsedModel = parsedModel;

        runOnLooper(new Runnable() {
            @Override
            public void run() {
                if (requestListener != null) {
                    requestListener.onComplete(response);
                }
            }
        });
    }

    protected void provideError(final OKError error) {
        error.request = this;

        runOnLooper(new Runnable() {
            @Override
            public void run() {
                if (requestListener != null) {
                    requestListener.onError(error);
                }
            }
        });

    }

    public static abstract class OKRequestListener {
        public void onComplete(OKResponse response) {
        }

        public void onError(OKError error) {
        }
    }

    private void runOnLooper(Runnable block) {
        runOnLooper(block, 0);
    }

    private void runOnLooper(Runnable block, int delay) {
        if (mLooper == null) {
            mLooper = Looper.getMainLooper();
        }
        if (delay > 0) {
            new Handler(mLooper).postDelayed(block, delay);
        } else {
            new Handler(mLooper).post(block);
        }
    }

    private void runOnMainLooper(Runnable block) {
        new Handler(Looper.getMainLooper()).post(block);
    }
}
