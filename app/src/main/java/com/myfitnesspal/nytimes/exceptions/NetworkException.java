package com.myfitnesspal.nytimes.exceptions;

import java.io.IOException;

/**
 * Created by tludewig on 8/20/17.
 */

public class NetworkException extends IOException {

    @Override
    public String getMessage() {
        return "No connectivity exception";
    }

}
