package com.backend.tutor_app.model.enums;

import lombok.Getter;

@Getter
public enum SocialProvider {
    GOOGLE("Google", "https://accounts.google.com/oauth/authorize"),
    FACEBOOK("Facebook", "https://www.facebook.com/v18.0/dialog/oauth"),
    GITHUB("GitHub", "https://github.com/login/oauth/authorize");

    private final String displayName;
    private final String authUrl;

    SocialProvider(String displayName, String authUrl) {
        this.displayName = displayName;
        this.authUrl = authUrl;
    }


}
