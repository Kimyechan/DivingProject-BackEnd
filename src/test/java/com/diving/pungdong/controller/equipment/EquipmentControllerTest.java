package com.diving.pungdong.controller.equipment;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.equipment.create.*;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.EquipmentService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
@ActiveProfiles("test")
class EquipmentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    AccountService accountService;

    @MockBean
    EquipmentService equipmentService;

    @MockBean
    LectureEsService lectureEsService;

    public Account createAccount() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new UserAccount(account));

        return account;
    }

    @Test
    @DisplayName("강의 대여장비 목록 생성")
    public void createLectureEquipments() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        EquipmentStockInfo equipmentStockInfo = EquipmentStockInfo.builder()
                .size("270")
                .quantity(10)
                .build();

        EquipmentInfo equipmentInfo = EquipmentInfo.builder()
                .name("오리발")
                .price(5000)
                .equipmentStockInfos(List.of(equipmentStockInfo))
                .build();

        EquipmentCreateInfo equipmentCreateInfo = EquipmentCreateInfo.builder()
                .lectureId(1L)
                .equipmentInfos(List.of(equipmentInfo))
                .build();

        EquipmentResult equipmentResult = EquipmentResult.builder()
                .equipmentId(1L)
                .name("오리발")
                .build();

        EquipmentCreateResult equipmentCreateResult = EquipmentCreateResult.builder()
                .lectureId(1L)
                .equipmentResults(List.of(equipmentResult))
                .build();

        given(equipmentService.saveRentEquipmentInfos(any(), any())).willReturn(equipmentCreateResult);

        mockMvc.perform(post("/equipment/create/list")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(equipmentCreateInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "equipment-create-list",
                                requestHeaders(
                                        headerWithName(org.springframework.http.HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(org.springframework.http.HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("equipmentInfos[].name").description("오리발"),
                                        fieldWithPath("equipmentInfos[].price").description("장비 한 개당 가격"),
                                        fieldWithPath("equipmentInfos[].equipmentStockInfos[].size").description("장비 한 종류의 사이즈"),
                                        fieldWithPath("equipmentInfos[].equipmentStockInfos[].quantity").description("장비 한 종류의 양")
                                ),
                                responseFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("equipmentResults[].equipmentId").description("장비 한 종류의 식별자 값"),
                                        fieldWithPath("equipmentResults[].name").description("장비 한 종류의 이름"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

}