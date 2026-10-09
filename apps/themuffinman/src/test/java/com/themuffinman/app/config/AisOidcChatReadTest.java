package com.themuffinman.app.config;

import com.themuffinman.app.chat.controller.AisChatStatusController;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.*;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(AisOidcChatReadTest.Fixture.class)
@WebAppConfiguration
@TestPropertySource(properties={"app.ais.oidc.enabled=true",
    "app.ais.oidc.issuer=https://identity.example.invalid/realms/ais-test",
    "app.ais.oidc.audience=themuffinman-chat",
    "app.ais.oidc.subject-account-ids[synthetic-shared-owner]=900001"})
class AisOidcChatReadTest {
    private static final String ISSUER="https://identity.example.invalid/realms/ais-test";
    private static final String SUBJECT="synthetic-shared-owner";
    private static final String SCOPE="ais.chat-status.read";
    private static final RSAKey KEY=newKey();
    private static final HttpServer JWKS=startJwks();
    @Autowired WebApplicationContext context;
    @Autowired AppUserRepository users;
    @Autowired ChatService chat;
    private MockMvc mvc;
    private AppUser owner;
    @DynamicPropertySource static void jwks(DynamicPropertyRegistry registry) {
        registry.add("app.ais.oidc.jwk-set-uri",()->"http://127.0.0.1:"+JWKS.getAddress().getPort()+"/jwks");
    }
    @AfterAll static void stop() { JWKS.stop(0); }
    @BeforeEach void setup() {
        reset(users,chat);
        owner=new AppUser();owner.setId(900001L);owner.setEmail("synthetic-member@example.invalid");owner.setRole(AppUserRole.USER);
        when(users.findById(900001L)).thenReturn(Optional.of(owner));
        when(chat.getWorkspace(owner,20,false)).thenReturn(ChatWorkspaceDTO.builder()
                .conversations(List.of()).contacts(List.of()).circles(List.of())
                .unreadConversationCount(2).onlineContactCount(1).conversationLimit(20).build());
        mvc=MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean("springSecurityFilterChain",Filter.class)).build();
    }
    @Test void signedTokenResolvesExistingAppUserAndReturnsOnlyAggregate() throws Exception {
        mvc.perform(get("/ais/chat-status").header("Authorization","Bearer "+validToken()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.component").value("TheMuffinMan"))
            .andExpect(jsonPath("$.unreadConversationCount").value(2))
            .andExpect(jsonPath("$.onlineContactCount").value(1))
            .andExpect(jsonPath("$.privateMessageBodiesReturned").value(false))
            .andExpect(jsonPath("$.mutations").value(0))
            .andExpect(jsonPath("$.email").doesNotExist()).andExpect(jsonPath("$.contacts").doesNotExist())
            .andExpect(jsonPath("$.conversations").doesNotExist());
        verify(chat).getWorkspace(owner,20,false);
        verify(users,never()).findByEmail(anyString());
    }
    @Test void unrelatedAudienceIssuerSignatureExpiryAndSubjectsNeverCallDomainService() throws Exception {
        String[] tokens={token(KEY,"https://other.example.invalid",SUBJECT,"themuffinman-chat",SCOPE,Instant.now().plusSeconds(300)),
            token(KEY,ISSUER,SUBJECT,"doomsday-shopping",SCOPE,Instant.now().plusSeconds(300)),
            token(newKey(),ISSUER,SUBJECT,"themuffinman-chat",SCOPE,Instant.now().plusSeconds(300)),
            token(KEY,ISSUER,SUBJECT,"themuffinman-chat",SCOPE,Instant.now().minusSeconds(180)),
            token(KEY,ISSUER,SUBJECT,"themuffinman-chat",SCOPE,null),
            token(KEY,ISSUER,"unlinked-owner","themuffinman-chat",SCOPE,Instant.now().plusSeconds(300)),
            token(KEY,ISSUER,"","themuffinman-chat",SCOPE,Instant.now().plusSeconds(300))};
        for(String t:tokens)mvc.perform(get("/ais/chat-status").header("Authorization","Bearer "+t)).andExpect(status().isUnauthorized());
        verifyNoInteractions(chat);
    }
    @Test void readScopeAndReadMethodAreRequired() throws Exception {
        mvc.perform(get("/ais/chat-status")).andExpect(status().isUnauthorized());
        mvc.perform(get("/ais/chat-status").header("Authorization","Bearer "+token(KEY,ISSUER,SUBJECT,"themuffinman-chat","unrelated",Instant.now().plusSeconds(300))))
            .andExpect(status().isForbidden());
        mvc.perform(post("/ais/chat-status").header("Authorization","Bearer "+validToken())).andExpect(status().isForbidden());
        mvc.perform(get("/ais/messages").header("Authorization","Bearer "+validToken())).andExpect(status().isForbidden());
        verifyNoInteractions(chat);
    }
    @Test void deletedLocalUserCannotFallBackToTokenEmailOrAdminRole() throws Exception {
        when(users.findById(900001L)).thenReturn(Optional.empty());
        mvc.perform(get("/ais/chat-status").header("Authorization","Bearer "+validToken())).andExpect(status().isUnauthorized());
        verify(users,never()).findByEmail(anyString());verifyNoInteractions(chat);
    }
    private static String validToken() throws Exception {return token(KEY,ISSUER,SUBJECT,"themuffinman-chat",SCOPE,Instant.now().plusSeconds(300));}
    private static String token(RSAKey key,String issuer,String subject,String audience,String scope,Instant expiry) throws Exception {
        var claims=new JWTClaimsSet.Builder().issuer(issuer).subject(subject).audience(audience)
            .issueTime(Date.from(Instant.now().minusSeconds(10))).claim("scope",scope)
            .claim("email","another-admin@example.invalid").claim("roles",List.of("ADMIN"));
        if(expiry!=null)claims.expirationTime(Date.from(expiry));
        var jwt=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),claims.build());
        jwt.sign(new RSASSASigner(key));return jwt.serialize();
    }
    private static RSAKey newKey() {
        try {var gen=KeyPairGenerator.getInstance("RSA");gen.initialize(2048);var pair=gen.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey)pair.getPublic()).privateKey((RSAPrivateKey)pair.getPrivate()).keyID("synthetic-shared-key").build();
        }catch(Exception e){throw new IllegalStateException(e);}
    }
    private static HttpServer startJwks() {
        try {var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            server.createContext("/jwks",exchange->{byte[] b=new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type","application/json");exchange.sendResponseHeaders(200,b.length);
                try(var out=exchange.getResponseBody()){out.write(b);}});server.start();return server;
        }catch(Exception e){throw new IllegalStateException(e);}
    }
    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import({AisOidcSecurityConfig.class,AisChatStatusController.class})
    static class Fixture {
        @Bean AppUserRepository users(){return mock(AppUserRepository.class);}
        @Bean ChatService chat(){return mock(ChatService.class);}
    }
}
