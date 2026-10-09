package com.themuffinman.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import jakarta.servlet.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfSystemProperty(named="ais.provider.tokens",matches=".+")
@SpringJUnitConfig(AisOidcChatReadTest.Fixture.class)
@WebAppConfiguration
@TestPropertySource(properties={"app.ais.oidc.enabled=true","app.ais.oidc.audience=themuffinman-chat",
    "app.ais.oidc.subject-account-ids[e5f37a83-65d7-4a3c-a5d2-5c5c9dcdd777]=900001"})
class AisSharedProviderReadTest {
    @Autowired WebApplicationContext context;
    @Autowired AppUserRepository users;
    @Autowired ChatService chat;
    @Autowired AisOidcProperties properties;
    private MockMvc mvc;
    private AppUser owner;
    @DynamicPropertySource static void provider(DynamicPropertyRegistry p) {
        p.add("app.ais.oidc.issuer",()->value("themuffinman-chat","issuer"));
        p.add("app.ais.oidc.jwk-set-uri",()->value("themuffinman-chat","jwks_uri"));
    }
    static String value(String client,String field) {
        try {return new ObjectMapper().readTree(Files.readString(Path.of(System.getProperty("ais.provider.tokens"))))
                .path(client).path(field).asText();}
        catch(Exception e){throw new IllegalStateException("Synthetic provider fixture unavailable",e);}
    }
    @BeforeEach void fixture() throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals("/private/tmp/ais-keycloak-provider-pilot/java-trust.p12",
                System.getProperty("javax.net.ssl.trustStore"), "Dedicated pilot truststore was not loaded into the test JVM");
        org.junit.jupiter.api.Assertions.assertEquals("rw-------",java.nio.file.attribute.PosixFilePermissions.toString(
                Files.getPosixFilePermissions(Path.of(System.getProperty("ais.provider.tokens")))));

        reset(users,chat);owner=new AppUser();owner.setId(900001L);owner.setRole(AppUserRole.USER);
        owner.setEmail("synthetic-real-provider@example.invalid");
        when(users.findById(900001L)).thenReturn(Optional.of(owner));
        properties.getSubjectAccountIds().put("e5f37a83-65d7-4a3c-a5d2-5c5c9dcdd777",900001L);
        when(chat.getWorkspace(owner,20,false)).thenReturn(ChatWorkspaceDTO.builder().conversations(List.of())
                .contacts(List.of()).circles(List.of()).conversationLimit(20).unreadConversationCount(2).onlineContactCount(1).build());
        mvc=MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean("springSecurityFilterChain",Filter.class)).build();
    }
    private String bearer(String client){return "Bearer "+value(client,"access_token");}
    @Test void realProviderTokenResolvesExistingAppUserAndReturnsPrivateCountsOnly() throws Exception {
        mvc.perform(get("/ais/chat-status").header("Authorization",bearer("themuffinman-chat")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.component").value("TheMuffinMan"))
            .andExpect(jsonPath("$.unreadConversationCount").value(2)).andExpect(jsonPath("$.mutations").value(0))
            .andExpect(jsonPath("$.privateMessageBodiesReturned").value(false))
            .andExpect(jsonPath("$.email").doesNotExist()).andExpect(jsonPath("$.contacts").doesNotExist());
        verify(chat).getWorkspace(owner,20,false);verify(users,never()).findByEmail(anyString());
    }
    @Test void otherServiceTokenAnonymousAndMutationsNeverReachChat() throws Exception {
        mvc.perform(get("/ais/chat-status").header("Authorization",bearer("doomsday-shopping"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/ais/chat-status")).andExpect(status().isUnauthorized());
        mvc.perform(post("/ais/chat-status").header("Authorization",bearer("themuffinman-chat"))).andExpect(status().isForbidden());
        verifyNoInteractions(chat);
    }
    @Test void unlinkedSubjectDoesNotGrantLocalAccount() throws Exception {
        properties.getSubjectAccountIds().clear();
        mvc.perform(get("/ais/chat-status").header("Authorization",bearer("themuffinman-chat"))).andExpect(status().isUnauthorized());
        verifyNoInteractions(chat);verifyNoInteractions(users);
    }
    @Test void deletedLocalAccountDoesNotFallBackToEmail() throws Exception {
        when(users.findById(900001L)).thenReturn(Optional.empty());
        mvc.perform(get("/ais/chat-status").header("Authorization",bearer("themuffinman-chat"))).andExpect(status().isUnauthorized());
        verify(users,never()).findByEmail(anyString());verifyNoInteractions(chat);
    }
}
