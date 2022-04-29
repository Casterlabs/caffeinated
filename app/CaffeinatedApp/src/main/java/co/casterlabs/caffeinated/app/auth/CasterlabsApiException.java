package co.casterlabs.caffeinated.app.auth;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class CasterlabsApiException extends Exception {
    private static final long serialVersionUID = 2348663608227120357L;

    @Getter
    private List<String> errors;

    public CasterlabsApiException(List<String> errors) {
        super(errors.toString());

        this.errors = Collections.unmodifiableList(errors);
    }

}
