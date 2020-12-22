package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoWishDataDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishRetDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import org.assertj.core.util.Lists;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MihoyoRestClientTest {

    private final String MIHOYO_ENDPOINT = "http://mihoyo-im-endpoint";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MihoyoRestClient mihoyoRestClient = new MihoyoRestClient(MIHOYO_ENDPOINT);

    @Test
    void givenMihoyoReturnsCorrectData_thenReturnWishes() throws URISyntaxException {
        URI uri = new URI(MIHOYO_ENDPOINT + "/event/gacha_info/api/getGachaLog?authkey=authkey&init_type=302&gacha_type=302&authkey_ver=1&sign_type=2&auth_appid=webview_gacha&lang=fr&size=20&page=0");

        List<MihoyoWishLogDTO> expectedWishes = Lists.newArrayList();
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();

        MihoyoWishDataDTO dataDTO = new MihoyoWishDataDTO();
        dataDTO.setList(expectedWishes);

        retDTO.setRetcode(1);
        retDTO.setData(dataDTO);

        Mockito
            .when(restTemplate.getForEntity(
                uri,
                MihoyoWishRetDTO.class))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        List<MihoyoWishLogDTO> wishes = mihoyoRestClient.getWishes("authkey", BannerType.WEAPON_EVENT, 0);

        Mockito.verify(restTemplate, Mockito.times(1)).getForEntity(
            Mockito.eq(uri),
            Mockito.eq(MihoyoWishRetDTO.class));

        assertThat(wishes).containsAll(expectedWishes);
    }

    @Test
    void givenMihoyoReturnsMinusOneCode_thenThrowApiError() throws URISyntaxException {
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();
        retDTO.setRetcode(-1);

        testForIncorrectData(retDTO);
    }

    @Test
    void givenMihoyoReturnsNullData_thenThrowApiError() throws URISyntaxException {
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();
        retDTO.setRetcode(1);

        testForIncorrectData(retDTO);
    }

    @Test
    void givenMihoyoReturnsNullList_thenThrowApiError() throws URISyntaxException {
        MihoyoWishRetDTO retDTO = new MihoyoWishRetDTO();
        retDTO.setRetcode(1);
        retDTO.setData(new MihoyoWishDataDTO());

        testForIncorrectData(retDTO);
    }

    private void testForIncorrectData(MihoyoWishRetDTO retDTO) throws URISyntaxException {
        URI uri = new URI(MIHOYO_ENDPOINT + "/event/gacha_info/api/getGachaLog?authkey=authkey&init_type=302&gacha_type=302&authkey_ver=1&sign_type=2&auth_appid=webview_gacha&lang=fr&size=20&page=0");

        Mockito
            .when(restTemplate.getForEntity(
                uri,
                MihoyoWishRetDTO.class))
            .thenReturn(new ResponseEntity<>(retDTO, HttpStatus.OK));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            mihoyoRestClient.getWishes("authkey", BannerType.WEAPON_EVENT, 0);
        });


        Mockito.verify(restTemplate, Mockito.times(1)).getForEntity(
            Mockito.eq(uri),
            Mockito.eq(MihoyoWishRetDTO.class));

        assertThat(exception).isExactlyInstanceOf(ApiError.class);
        assertThat(((ApiError) exception).getErrorType()).isEqualTo(ErrorType.AUTHKEY_INVALID);
    }
}
