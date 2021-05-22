package de.neemann.digital.plugin;

/**
 * Abstraction allowing asynchronous events upon incoming data
 */
public interface DataAvailInterface {

    /**
     * Triggered when new data is received
     */
    void onDataAvailable();

}
