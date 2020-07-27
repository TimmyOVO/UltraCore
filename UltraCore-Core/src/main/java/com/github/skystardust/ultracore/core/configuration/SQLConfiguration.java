package com.github.skystardust.ultracore.core.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SQLConfiguration {
    private String url;
    private String driver;
    private String username;
    private String password;
    private Boolean autoCommit;

    private SQLConfiguration(Builder builder) {
        setUrl(builder.url);
        setDriver(builder.driver);
        setUsername(builder.username);
        setPassword(builder.password);
        setAutoCommit(builder.autoCommit);
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private String url;
        private String driver;
        private String username;
        private String password;
        private Boolean autoCommit;

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
        public Builder withAutoCommit(@Nonnull Boolean val) {
            autoCommit = val;
            return this;
        }

        @Nonnull
        public SQLConfiguration build() {
            return new SQLConfiguration(this);
        }
    }
}
