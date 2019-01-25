package com.github.skystardust.ultracore.core.configuration;

import javax.annotation.Nonnull;

public class SQLConfiguration {
    private String url;
    private String driver;
    private String username;
    private String password;

    public SQLConfiguration(String url, String driver, String username, String password) {
        this.url = url;
        this.driver = driver;
        this.username = username;
        this.password = password;
    }

    public SQLConfiguration() {
    }

    private SQLConfiguration(Builder builder) {
        setUrl(builder.url);
        setDriver(builder.driver);
        setUsername(builder.username);
        setPassword(builder.password);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static final class Builder {
        private String url;
        private String driver;
        private String username;
        private String password;

        private Builder() {
        }

        @Nonnull
        public Builder withUrl(@Nonnull String val) {
            url = val;
            return this;
        }

        @Nonnull
        public Builder withDriver(@Nonnull String val) {
            driver = val;
            return this;
        }

        @Nonnull
        public Builder withUsername(@Nonnull String val) {
            username = val;
            return this;
        }

        @Nonnull
        public Builder withPassword(@Nonnull String val) {
            password = val;
            return this;
        }

        @Nonnull
        public SQLConfiguration build() {
            return new SQLConfiguration(this);
        }
    }
}
