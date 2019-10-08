package dy.http;

import dy.log.AppLogger;
import lombok.Data;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/6/21.
 */
@Component("httpposter")
@Scope("prototype")
@Data
public class HttpPoster {

    @Autowired
    AppLogger appLogger;

    @Value("${httpretrynum}")
    int httpretrynum;

    @Async
    public void postJson(String postUrl, String postContent) throws Exception {
        OkHttpClient client=new OkHttpClient
                .Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS).addInterceptor(new RetryIntercepter(httpretrynum))
                .retryOnConnectionFailure(true).build();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),postContent);
        Request request = new Request.Builder().post(requestBody).url(postUrl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                appLogger.getLogger().error(call.toString() + e.getMessage() + postContent);
                System.out.println(call.request().toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
                appLogger.getLogger().info(postUrl + " success data : " + postContent);
            }
        });
    }


}
