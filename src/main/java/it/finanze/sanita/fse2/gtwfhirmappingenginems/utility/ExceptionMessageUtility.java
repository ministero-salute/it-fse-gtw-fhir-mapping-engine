package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Produces nonblank diagnostics without exposing exception stack traces. */
public final class ExceptionMessageUtility {

    private ExceptionMessageUtility() {}

    public static String describe(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = failure; current != null && visited.add(current); current = current.getCause()) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }

        String className = "";
        if (failure != null) {
            className = failure.getClass().getName();
        }

        return "CDA-to-FHIR processing failed: " + className + " (no diagnostic message available).";
    }
}
