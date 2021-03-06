package com.foreks.atakanbodur.starter;

import com.foreks.atakanbodur.starter.entities.LogObject;
import com.foreks.atakanbodur.starter.entities.OpenLogFile;
import com.foreks.atakanbodur.starter.handlers.GenericHandler;
import com.foreks.atakanbodur.starter.handlers.DetailSearchHandler;
import com.foreks.atakanbodur.starter.repositories.LogObjectRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;


/*
TODO:
1. servis

/api/logs/summary?startDate=dd/MM/yyyy&endDate=dd/MM/YYYY&username=ATAKAN

iki tarih alacak bu tarihler arasında ilgili kayda ait sorgularla ilgili istatistik dönecek

input parametreleri:
startDate
EnDate;
user:

repsonse:
{
"totalRequest": 369,
"averageProcessTime": 11,
"totalSuccess": 330,
"totalFailure": 39,
"info": [

],
}

not: query'de user yoksa ilgili tarihteki bütün kayıtlar söz konusu olur.
*/
/*
TODO:
Bulk operations
 */
/*
TODO:
logger
 */
/*
TODO:
http response kodları
 */
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // init and configure client
    JsonObject config = new JsonObject().put("connection_string", "mongodb://192.168.0.137:27017").put("db_name", "logInfoProject");
    MongoClient client = MongoClient.createShared(vertx, config);
    //init file and exec reading/writing logs
    OpenLogFile openLogFile = new OpenLogFile("dummylogfile-org.txt", new OpenOptions(), vertx, client);
    openLogFile.execute();
    //init repository
    LogObjectRepository logObjectRepository = new LogObjectRepository(client);
    //init handlers
    GenericHandler genericHandler = new GenericHandler(logObjectRepository);
    DetailSearchHandler detailSearchHandler = new DetailSearchHandler(logObjectRepository);

    //init api routes
    Router router = Router.router(vertx);
    router.route("/api/logs*").handler(BodyHandler.create());
    router.get("/api/logs").handler(genericHandler::readAll);
    router.get("/api/logs/company/:company").handler(genericHandler::readByCompany);
    router.get("/api/logs/user/:user").handler(genericHandler::readByUser);
    router.get("/api/logs/method/:method").handler(genericHandler::readByMethod);
    router.get("/api/logs/statusCode/:statusCode").handler(genericHandler::readByStatusCode);
    router.get("/api/logs/processTimeMS/:processTimeMS").handler(genericHandler::readByProcessTimeMS);
    router.get("/api/logs/protocol/:protocol").handler(genericHandler::readByProtocol);
    router.get("/api/logs/port/:port").handler(genericHandler::readByPort);
    router.get("/api/logs/host/:host").handler(genericHandler::readByHost);
    router.get("/api/logs/resource/:resource").handler(genericHandler::readByResource);
    router.get("/api/logs/platform/:platform").handler(genericHandler::readByPlatform);
    router.get("/api/logs/appName/:appName").handler(genericHandler::readByAppName);
    router.get("/api/logs/appVersion/:appVersion").handler(genericHandler::readByAppVersion);
    router.get("/api/logs/detail").handler(detailSearchHandler::read);

    vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on 8080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
