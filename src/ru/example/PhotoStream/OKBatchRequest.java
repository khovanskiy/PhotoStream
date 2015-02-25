package ru.example.PhotoStream;


public class OKBatchRequest {
    private final OKRequest[] mRequests;
    private final OKResponse[] mResponses;
    public OKBatchRequestListener requestListener;

    public OKBatchRequest(OKRequest... requests) {
        mRequests = requests;
        mResponses = new OKResponse[mRequests.length];
    }

    public void executeWithListener(OKBatchRequestListener listener) {
        this.requestListener = listener;

        for (OKRequest request : mRequests) {
            final OKRequest.OKRequestListener originalListener = request.requestListener;
            request.setRequestListener(new OKRequest.OKRequestListener() {
                @Override
                public void onComplete(OKResponse response) {
                    if (originalListener != null) {
                        originalListener.onComplete(response);
                    }
                    provideResponse(response);
                }

                @Override
                public void onError(OKError error) {
                    if (originalListener != null) {
                        originalListener.onError(error);
                    }
                    provideError(error);
                }
            });
            request.execute();
        }

    }

    protected void provideResponse(OKResponse response) {
        mResponses[indexOfRequest(response.request)] = response;
        for (OKResponse resp : mResponses) {
            if (resp == null) {
                return;
            }
        }

        if (requestListener != null) {
            requestListener.onComplete(mResponses);
        }
    }

    private int indexOfRequest(OKRequest request) {
        for (int i = 0; i < mRequests.length; ++i) {
            if (mRequests[i].equals(request)) {
                return i;
            }
        }
        return -1;
    }

    protected void provideError(OKError error) {
        if (requestListener != null) {
            requestListener.onError(error);
        }
    }

    public static abstract class OKBatchRequestListener {
        /**
         * Called if there were no HTTP or API errors, returns execution result.
         *
         * @param responses responses from OKRequests in passing order of construction
         */
        public void onComplete(OKResponse[] responses) {
        }

        /**
         * Called immediately if there was API error, or after <b>attempts</b> tries if there was an HTTP error
         *
         * @param error error for OKRequest
         */
        public void onError(OKError error) {
        }
    }
}
