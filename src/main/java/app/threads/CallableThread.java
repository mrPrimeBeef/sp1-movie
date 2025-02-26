package app.threads;



import app.utils.ApiReader;

import java.util.concurrent.Callable;

public class CallableThread implements Callable<String> {
    String url;

    public CallableThread(String url) {
        this.url = url;
    }

    @Override
    public String call() throws Exception {
       return ApiReader.getDataFromUrl(this.url);
    }
}