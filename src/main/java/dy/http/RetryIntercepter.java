package dy.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RetryIntercepter implements Interceptor {
    public int maxRetry;//最大重试次数
    private int retryNum = 0;//假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

    public RetryIntercepter(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        boolean responseOk = false;
        while (!responseOk && retryNum < maxRetry){
            try {
                response = chain.proceed(request);
                responseOk = response.isSuccessful();

            }catch (Exception ex){
                ex.printStackTrace();
            }finally {
                retryNum ++;
            }
        }
        return response;
    }

}
