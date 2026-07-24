package com.mvp.transport;

import java.io.IOException;

public interface Transport {

    void start() throws IOException;

    void stop();
}