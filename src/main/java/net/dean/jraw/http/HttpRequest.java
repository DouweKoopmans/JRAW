package net.dean.jraw.http;

import net.dean.jraw.JrawUtils;
import org.codehaus.jackson.JsonNode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * This class provides a way to consolidate all the main attributes of a RESTful HTTP request into one object
 */
public class HttpRequest {
    /** The HTTP verb to use to execute the request */
    private final HttpVerb verb;
    /** The path relative to the root of the host */
    private final String path;
    /** The arguments to be passed either by query string if the method is GET or DELETE, or by form if it is a different request */
    private final Map<String, String> args;
    private final String hostname;
    /** The time this request was executed */
    private LocalDateTime executed;
    private final JsonNode json;
    private final boolean isJson;

    /**
     * Instantiates a simple RestRequest
     *
     * @param verb The HTTP verb to use
     * @param hostname The host to use. For example, "reddit.com" or "ssl.reddit.com"
     * @param path The path of the request. For example, "/api/login".
     */
    public HttpRequest(HttpVerb verb, String hostname, String path) {
        this(new Builder(verb, hostname, path));
    }

    /**
     * Instantiates a new HttpRequest
     * @param b The Builder to use
     */
    protected HttpRequest(Builder b) {
        this.verb = b.verb;
        this.hostname = b.hostname;
        this.path = b.path;
        this.args = b.args;
        this.json = b.json;
        this.isJson = b.json != null;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    /**
     * Get the time that this request was executed. Mainly used for ratelimiting.
     *
     * @return The time this request was executed, or null if it hasn't been executed yet.
     * @see net.dean.jraw.RedditClient#setRequestManagementEnabled(boolean)
     */
    public LocalDateTime getExecuted() {
        return executed;
    }

    public HttpVerb getVerb() {
        return verb;
    }

    public String getHostname() {
        return hostname;
    }

    public JsonNode getJson() {
        return json;
    }

    /**
     * Checks if this request contains JSON data.
     * @return False if the HTTP verb is GET or DELETE or no JsonNode was passed to this request's Builder.
     */
    public boolean isJson() {
        return isJson;
    }

    /**
     * Called when this request is executed to take note of the current time.
     * @throws IllegalStateException If this method has been called more than once
     */
    public void onExecuted() {
        if (executed != null) {
            throw new IllegalStateException("Already executed (" + executed + ")");
        }
        this.executed = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "HttpRequest {" +
                "verb=" + verb +
                ", path='" + path + '\'' +
                ", args=" + args +
                ", hostname='" + hostname + '\'' +
                ", executed=" + executed +
                '}';
    }

    /**
     * This class is responsible for building HttpRequest objects. When extending this class where {@literal <T>} is not
     * {@link HttpRequest}, you <b>must</b> override {@link #build()} lest your application will become flooded with
     * ClassCastExceptions.
     *
     * @param <T> The type of HttpRequest to return
     * @param <U> The type of Builder to return in {@link #build()}
     */
    public static class Builder<T extends HttpRequest, U extends Builder<T, U>> {
        protected final HttpVerb verb;
        protected final String hostname;
        protected final String path;
        protected Map<String, String> args;
        protected JsonNode json;

        /**
         * Instantiates a new Builder
         * @param verb The HTTP verb to use
         * @param hostname The host to use. For example, "reddit.com" or "ssl.reddit.com"
         * @param path The path of the request. For example, "/api/login".
         */
        public Builder(HttpVerb verb, String hostname, String path) {
            this.verb = verb;
            this.hostname = hostname;
            this.path = path;
        }

        /**
         * Sets the query args for GET and DELETE requests or the form args for other HTTP verbs.
         * @param args The arguments to use
         * @return This Builder
         */
        @SuppressWarnings("unchecked")
        public U args(Map<String, String> args) {
            this.args = args;
            return (U) this;
        }

        /**
         * Sets the query args for GET and DELETE requests or the form args for other HTTP verbs. The Object array must
         * meet the requirements described in {@link net.dean.jraw.JrawUtils#args(Object...)} for this method to complete
         * successfully.
         * @param args The arguments to use
         * @return This Builder
         */
        @SuppressWarnings("unchecked")
        public U args(Object... args) {
            this.args = JrawUtils.args(args);
            return (U) this;
        }

        /**
         * Sets the JSON data. Only applicable for HTTP verbs that support form arguments such as POST.
         * @param json The JSON data to use
         * @return This Builder
         */
        public Builder json(JsonNode json) {
            if (verb == HttpVerb.GET || verb == HttpVerb.DELETE) {
                throw new IllegalArgumentException("Can't have JSON in a query string (you tried to attach a JsonNode to an " +
                        "HTTP verb that doesn't support application/x-www-form-urlencoded data: " + verb + ")");
            }
            this.json = json;
            return this;
        }

        /**
         * Instantiates a new HttpRequest or one of its subclasses
         * @return A new HttpRequest
         */
        public T build() {
            return (T) new HttpRequest(this);
        }
    }
}