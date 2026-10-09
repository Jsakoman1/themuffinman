package com.themuffinman.app.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ais.oidc")
public class AisOidcProperties {
    private boolean enabled;
    private String issuer;
    private String audience;
    private String jwkSetUri;
    private Map<String, Long> subjectAccountIds = new LinkedHashMap<>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getJwkSetUri() { return jwkSetUri; }
    public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }
    public Map<String, Long> getSubjectAccountIds() { return subjectAccountIds; }
    public void setSubjectAccountIds(Map<String, Long> subjectAccountIds) { this.subjectAccountIds = subjectAccountIds; }
}
