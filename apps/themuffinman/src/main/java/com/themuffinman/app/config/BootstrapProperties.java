package com.themuffinman.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class BootstrapProperties {

    private final Seed seed = new Seed();
    private final UserCredentials admin = new UserCredentials();
    private final UserCredentials test = new UserCredentials();
    private final Bootstrap bootstrap = new Bootstrap();

    @Getter
    @Setter
    public static class Seed {
        private final Users users = new Users();
    }

    @Getter
    @Setter
    public static class Users {
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class Bootstrap {
        private final Admin admin = new Admin();
    }

    @Getter
    @Setter
    public static class Admin {
        private boolean enabled;
        private String email;
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class UserCredentials {
        private String email;
        private String username;
        private String password;
    }
}
