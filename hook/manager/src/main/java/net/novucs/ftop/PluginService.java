package net.novucs.ftop;

/**
 * A standard service, allowing dynamic enabling and disabling of specific
 * plugin mechanics while the plugin is running.
 */
public interface PluginService {

    /**
     * Initializes the service.
     */
    void initialize();

    /**
     * Terminates the service.
     */
    void terminate();

}
