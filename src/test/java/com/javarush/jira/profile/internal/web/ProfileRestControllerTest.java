package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.User;
import com.javarush.jira.login.internal.UserRepository;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.javarush.jira.profile.internal.web.ProfileTestData.*;

class ProfileRestControllerTest extends AbstractControllerTest {

    @Autowired
    private ProfileRestController profileRestController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    protected ProfileMapper profileMapper;

    private AuthUser authUser;
    private ProfileTo profileTo;

    private static final String REST_URL = ProfileRestController.REST_URL;

    @BeforeEach
    public void setup() {
        User user = getNewUser(1L);

        userRepository.save(user);
        authUser = new AuthUser(user);
        profileTo = profileMapper.toTo(getNew(1L));    }


    @Test
    public void getTestUtil() {
        User existed = userRepository.getExistedByEmail("doe@mail.com");
        ProfileTo profile = profileRestController.get(authUser);

        Assertions.assertEquals(profile.getId(), existed.getId());
    }

    @Test
    public void getTest() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL)
                .with(SecurityMockMvcRequestPostProcessors.user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void getUnAuthorizedTest() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void putTest() throws Exception {
        ProfileTo updatedProfileTo = profileMapper.toTo(getUpdated(profileTo.getId()));

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProfileTo))
                .with(SecurityMockMvcRequestPostProcessors.user(authUser)))
                .andExpect(status().isNoContent());

        Profile fromRepo = profileRepository.getOrCreate(profileTo.getId());

        Assertions.assertEquals(profileMapper.toTo(fromRepo).getMailNotifications(), updatedProfileTo.getMailNotifications());
    }

    @Test
    public void putInvalidProfileToTest() throws Exception {
        ProfileTo invalidProfileTo = getInvalidTo();

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProfileTo))
                .with(SecurityMockMvcRequestPostProcessors.user(authUser)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void putUnAuthorizedTest() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL, objectMapper.writeValueAsString(getNewTo()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}