package co.casterlabs.caffeinated.util;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * Marks any field/class/value as something that is invoked/get/set by
 * reflection and not by direct calling.
 */
@Retention(SOURCE)
public @interface Reflective {

}
