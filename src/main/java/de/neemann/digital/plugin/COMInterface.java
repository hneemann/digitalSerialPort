package de.neemann.digital.plugin;

/**
 * Abstraction of a COM interface
 */
public interface COMInterface {
    /**
     * Write a byte to the com port
     *
     * @param data the data to write
     */
    void write(int data);

    /**
     * Reads a byte from the comm interface
     *
     * @return the byte or -1 if no data is available
     */
    int readData();

    /**
     * Closes the port.
     */
    void close();
}
