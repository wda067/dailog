package com.dailog.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.dailog.api.config.CustomMockOAuth2Account;
import com.dailog.api.repository.member.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @CustomMockOAuth2Account(provider = "naver")
    void test() {
    }
}