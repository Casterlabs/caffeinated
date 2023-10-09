package co.casterlabs.caffeinatedapi.routes;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.rakurai.io.http.HttpMethod;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.io.http.server.HttpSession;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

public class SpeedTestRoute implements HttpProvider {
    private static final int LIMIT = 200/*mb*/ * 1000 * 1000;

    @HttpEndpoint(uri = "/public/v2/caffeinated/speedtest", allowedMethods = {
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.OPTIONS
    })
    public HttpResponse onSpeedTest(HttpSession session) throws IOException {
        switch (session.getMethod()) {
            case OPTIONS:
                return HttpResponse
                    .newFixedLengthResponse(StandardHttpStatus.NO_CONTENT)
                    .putHeader("Access-Control-Allow-Origin", "*")
                    .putHeader("Access-Control-Allow-Methods", "*")
                    .putHeader("Access-Control-Allow-Headers", "*");

            case GET:
                return HttpResponse
                    .newFixedLengthResponse(
                        StandardHttpStatus.OK,
                        new JsonObject()
                            .put(
                                "data",
                                new JsonObject()
                                    .put("limit", LIMIT)
                                    .put("recommendedDownload", LIMIT / 2) // 100mb
                                    .put("recommendedUpload", LIMIT / 40) // 5mb
                            )
                            .put("errors", JsonArray.EMPTY_ARRAY)
                    )
                    .putHeader("Access-Control-Allow-Origin", "*")
                    .putHeader("Access-Control-Allow-Methods", "*")
                    .putHeader("Access-Control-Allow-Headers", "*");

            case PUT: {
                {
                    long total = 0;
                    long read;
                    while ((read = session.getRequestBodyStream().skip(2048)) != -1) {
                        total += read;
                        if (total > LIMIT) {
                            // Over the limit.
                            return HttpResponse
                                .newFixedLengthResponse(StandardHttpStatus.BAD_REQUEST)
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .putHeader("Access-Control-Allow-Methods", "*")
                                .putHeader("Access-Control-Allow-Headers", "*");
                        }
                    }
                }

                return HttpResponse
                    .newFixedLengthResponse(StandardHttpStatus.OK)
                    .putHeader("Access-Control-Allow-Origin", "*")
                    .putHeader("Access-Control-Allow-Methods", "*")
                    .putHeader("Access-Control-Allow-Headers", "*");
            }

            case POST: {
                int amount = Integer.parseInt(session.getQueryParameters().getOrDefault("size", Integer.toString(LIMIT)));

                if (amount > LIMIT) {
                    return HttpResponse.newFixedLengthResponse(StandardHttpStatus.BAD_REQUEST);
                }

                return HttpResponse
                    .newFixedLengthResponse(
                        StandardHttpStatus.CREATED,
                        new InputStream() {
                            private int remaining = amount;

                            @Override
                            public int read() throws IOException {
                                if (this.remaining == 0) return -1;

                                this.remaining -= 1;
                                return 0;
                            }

                            @Override
                            public int read(byte[] b, int off, int len) throws IOException {
                                if (this.remaining == 0) return -1;

                                len = Math.min(len, this.remaining);
                                this.remaining -= len;
                                return len;
                            }
                        },
                        amount
                    )
                    .putHeader("Access-Control-Allow-Origin", "*")
                    .putHeader("Access-Control-Allow-Methods", "*")
                    .putHeader("Access-Control-Allow-Headers", "*");
            }

            default:
                return null;
        }
    }

}
