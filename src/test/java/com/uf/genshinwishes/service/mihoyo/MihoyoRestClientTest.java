package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoWishDataDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishRetDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MihoyoRestClientTest {

    @Mock
    private MihoyoGameBizSettingsSelector selector;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MihoyoRestClient mihoyoRestClient = new MihoyoRestClient();

    @BeforeEach
    public void before() {
        Mockito.when(selector.getWishEndpoint("hk4e_global"))
            .thenReturn(Optional.of("http://mihoyo-im-endpoint"));
    }

    @Test
    void givenMihoyoReturnsCorrectData_thenReturnWishes() {
        List<MihoyoWishLogDTO> expectedWishes = Lists.newArrayList();
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();

        MihoyoWishDataDTO dataDTO = new MihoyoWishDataDTO();
        dataDTO.setList(expectedWishes);

        retDTO.setRetcode(1);
        retDTO.setData(dataDTO);

        Mockito
            .when(restTemplate.getForEntity(Mockito.any(), Mockito.any()))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        List<MihoyoWishLogDTO> wishes = mihoyoRestClient.getWishes("authkey", "hk4e_global", BannerType.WEAPON_EVENT, "last-id", 0);

        assertThat(wishes).containsAll(expectedWishes);
    }

    @Test
    void givenMihoyoReturnsMinusOneCode_thenThrowApiError() {
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();
        retDTO.setRetcode(-1);

        testForIncorrectData(retDTO);
    }

    @Test
    void givenMihoyoReturnsNullData_thenThrowApiError() {
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();
        retDTO.setRetcode(1);

        testForIncorrectData(retDTO);
    }

    @Test
    void givenMihoyoReturnsNullList_thenThrowApiError() {
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();
        retDTO.setRetcode(1);
        retDTO.setData(new MihoyoWishDataDTO());

        testForIncorrectData(retDTO);
    }

    private void testForIncorrectData(MihoyoWishRetDTO retDTO) {
        Mockito
            .when(restTemplate.getForEntity(Mockito.any(), Mockito.any()))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        Exception exception = assertThrows(
            RuntimeException.class,
            () -> mihoyoRestClient.getWishes("authkey", "hk4e_global", BannerType.WEAPON_EVENT, "last_id", 0));

        assertThat(exception).isExactlyInstanceOf(ApiError.class);
        assertThat(exception).isInstanceOfSatisfying(
            ApiError.class,
            e -> assertThat(e.getErrorType()).isEqualTo(ErrorType.AUTHKEY_INVALID));
    }
}
