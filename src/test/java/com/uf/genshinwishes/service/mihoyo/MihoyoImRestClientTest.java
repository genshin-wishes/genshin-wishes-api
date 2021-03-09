package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoInfoRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MihoyoImRestClientTest {

    private final String MIHOYO_ENDPOINT = "http://mihoyo-im-endpoint";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MihoyoImRestClient mihoyoImRestClient = new MihoyoImRestClient(MIHOYO_ENDPOINT);

    @Test
    void givenMihoyoReturnsMinusOneCode_thenThrowApiError() throws URISyntaxException {
        URI uri = new URI(MIHOYO_ENDPOINT + "/common/im/userClient/initUserChat?authkey=authkey&authkey_ver=1&game_biz=hk4e_global&sign_type=2");
        MihoyoInfoRetDTO retDTO = new MihoyoInfoRetDTO();
        retDTO.setRetcode(-1);

        Mockito
            .when(restTemplate.postForEntity(
                uri,
                "{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}",
                MihoyoInfoRetDTO.class))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            mihoyoImRestClient.getUserInfo(Optional.empty(), "authkey");
        });


        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(
            Mockito.eq(uri),
            Mockito.eq("{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}"),
            Mockito.eq(MihoyoInfoRetDTO.class));

        assertThat(exception).isExactlyInstanceOf(ApiError.class);
        assertThat(((ApiError) exception).getErrorType()).isEqualTo(ErrorType.AUTHKEY_INVALID);
    }

    @Test
    void givenMihoyoReturnsNullData_thenThrowApiError() throws URISyntaxException {
        URI uri = new URI(MIHOYO_ENDPOINT + "/common/im/userClient/initUserChat?authkey=authkey&authkey_ver=1&game_biz=hk4e_global&sign_type=2");
        MihoyoInfoRetDTO retDTO = new MihoyoInfoRetDTO();
        retDTO.setRetcode(1);

        Mockito
            .when(restTemplate.postForEntity(
                uri,
                "{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}",
                MihoyoInfoRetDTO.class))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            mihoyoImRestClient.getUserInfo(Optional.empty(), "authkey");
        });


        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(
            Mockito.eq(uri),
            Mockito.eq("{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}"),
            Mockito.eq(MihoyoInfoRetDTO.class));

        assertThat(exception).isExactlyInstanceOf(ApiError.class);
        assertThat(((ApiError) exception).getErrorType()).isEqualTo(ErrorType.AUTHKEY_INVALID);
    }

    @Test
    void givenMihoyoReturnsCorrectData_thenReturnMihoyoUserDtoWithUidAndUsername() throws URISyntaxException {
        URI uri = new URI(MIHOYO_ENDPOINT + "/common/im/userClient/initUserChat?authkey=authkey&authkey_ver=1&game_biz=hk4e_global&sign_type=2");
        MihoyoUserDTO userDTO = new MihoyoUserDTO();
        userDTO.setUser_id("some user id");
        userDTO.setNickname("some username");
        MihoyoInfoRetDTO retDTO = new MihoyoInfoRetDTO();
        retDTO.setRetcode(1);
        retDTO.setData(userDTO);

        Mockito
            .when(restTemplate.postForEntity(
                uri,
                "{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}",
                MihoyoInfoRetDTO.class))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        MihoyoUserDTO userDto = mihoyoImRestClient.getUserInfo(Optional.empty(), "authkey");


        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(
            Mockito.eq(uri),
            Mockito.eq("{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}"),
            Mockito.eq(MihoyoInfoRetDTO.class));

        assertThat(userDto).isEqualTo(userDTO);
    }
}
