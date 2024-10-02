package co.casterlabs.caffeinated.pluginsdk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.promise.Promise;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Currencies {
    private static final long UPDATE_INTERVAL = TimeUnit.HOURS.toMillis(1);

    private static List<CurrencyInfo> currencies = new ArrayList<>();
    private static List<String> psuedoCurrencies = new ArrayList<>();
    public static final String baseCurrency = "USD";

    static {
        AsyncTask.create(() -> {
            while (true) {
                try {
                    JsonObject response = Rson.DEFAULT.fromJson(
                        WebUtil.sendHttpRequest(new Request.Builder().url("https://api.casterlabs.co/v3/currencies")),
                        JsonObject.class
                    ).getObject("data");

                    psuedoCurrencies = Rson.DEFAULT.fromJson(response.get("psuedoCurrencies"), new TypeToken<List<String>>() {
                    });

                    currencies = Rson.DEFAULT.fromJson(response.get("currencies"), new TypeToken<List<CurrencyInfo>>() {
                    });

//                    baseCurrency = response.getString("baseCurrency");

                    FastLogger.logStatic(LogLevel.DEBUG, "Successfully updated currency info.");

                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst updating the currency info.");
                    FastLogger.logException(e);

                    // We want to retry VERY quickly.
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(15));
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    public static List<CurrencyInfo> getCurrencies() {
        return new ArrayList<>(currencies);
    }

    public static List<String> getPsuedoCurrencies() {
        return new ArrayList<>(psuedoCurrencies);
    }

    /**
     * @return An HTML formatted string to be displayed to the user.
     */
    public static Promise<String> formatCurrency(double amount, @NonNull String currency) {
        return new Promise<>(() -> {
            String response = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url(
                        String.format("https://api.casterlabs.co/v3/currencies/format?currency=%s&amount=%f", currency, amount)
                    )
            );

            if (response.startsWith("{")) {
                throw new ApiException(
                    Rson.DEFAULT
                        .fromJson(response, JsonObject.class)
                        .getArray("errors")
                        .toString()
                );
            } else {
                return response;
            }
        });
    }

    public static Promise<Double> convertCurrency(double amount, @NonNull String from, @NonNull String to) {
        return new Promise<>(() -> {
            String response = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url(
                        String.format("https://api.casterlabs.co/v3/currencies/convert?from=%s&to=%s&amount=%f&formatResult=false", from, to, amount)
                    )
            );

            if (response.startsWith("{")) {
                throw new ApiException(
                    Rson.DEFAULT
                        .fromJson(response, JsonObject.class)
                        .getArray("errors")
                        .toString()
                );
            } else {
                return Double.parseDouble(response);
            }
        });
    }

    /**
     * @return An HTML formatted string to be displayed to the user.
     */
    public static Promise<String> convertAndFormatCurrency(double amount, @NonNull String from, @NonNull String to) {
        return new Promise<>(() -> {
            String response = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url(
                        String.format("https://api.casterlabs.co/v3/currencies/convert?from=%s&to=%s&amount=%f&formatResult=true", from, to, amount)
                    )
            );

            if (response.startsWith("{")) {
                throw new ApiException(
                    Rson.DEFAULT
                        .fromJson(response, JsonObject.class)
                        .getArray("errors")
                        .toString()
                );
            } else {
                return response;
            }
        });
    }

    @Getter
    @ToString
    @JsonClass(exposeAll = true)
    public static class CurrencyInfo {
        private String currencyName;
        private String currencyCode;
        private String locale;

        @Override
        public int hashCode() {
            return this.currencyCode.hashCode();
        }

    }

}
