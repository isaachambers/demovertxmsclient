package io.vertx.book.http;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.*;
import io.vertx.rxjava.ext.web.client.*;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import rx.Single;

public class HelloConsumerMicroservice extends AbstractVerticle {

    private WebClient client;

    @Override
    public void start() {

        client = WebClient.create(vertx);
        Router router = Router.router(vertx);
        router.get("/").handler(this::callAnotherService);

        vertx.createHttpServer().requestHandler(router::accept).listen(7778, httpServerAsyncResult -> {
            System.out.println(httpServerAsyncResult.succeeded() + "httpServerAsyncResult ");
        });
    }

    public void callAnotherService(RoutingContext rc){
        HttpRequest<JsonObject> request1 = client
                .get(7777, "localhost", "/Luke")
                .as(BodyCodec.jsonObject());
        HttpRequest<JsonObject> request2 = client
                .get(7777, "localhost", "/Leia")
                .as(BodyCodec.jsonObject());
        Single<JsonObject> s1 = request1.rxSend()
                .map(HttpResponse::body);
        Single<JsonObject> s2 = request2.rxSend()
                .map(HttpResponse::body);
        Single.zip(s1, s2, (luke, leia) -> {
                   return new JsonObject().put("Luke", luke.getString("message"))
                    .put("Leia", leia.getString("message"));
        }).
                subscribe(result -> rc.response().end(result.encodePrettily()),error -> { error.printStackTrace();
                    rc.response().setStatusCode(500).end(error.getMessage());});
    }

}
