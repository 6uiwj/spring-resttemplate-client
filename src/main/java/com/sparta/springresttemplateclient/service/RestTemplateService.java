package com.sparta.springresttemplateclient.service;

import com.sparta.springresttemplateclient.dto.ItemDto;
import com.sparta.springresttemplateclient.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RestTemplateService {

    private final RestTemplate restTemplate;

    public RestTemplateService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }



    public ItemDto getCallObject(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070") //보낼 서버의 주소
                .path("/api/server/get-call-obj") //보낼 서버의 컨트롤러 부분
                .queryParam("query", query) //requestParam 방식이므로 url에 쿼리를 보내야 함
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);
        //responseEntity: Http 관련 데이터를 퐇마한 Reponse 응답을 할 때 사용
        //getForEntity(uri, 응답을 받아 변환하고 싶은 객체(역직렬화)) : get방식으로 해당 uri 서버에 요청, 받은 데이터를 파라미터로 넣어준 클래스 타입으로 변환해줌
        ResponseEntity<ItemDto> responseEntity = restTemplate.getForEntity(uri, ItemDto.class);

        log.info("statusCode = " + responseEntity.getStatusCode());
        //Body안에 가져온 데이터가 담겨있음
        return responseEntity.getBody();
    }

    public List<ItemDto> getCallList() {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/get-call-list")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        //이번엔 그냥 String으로 받을 것임. (복합적인 데이터라서)
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        log.info("statusCode = " + responseEntity.getStatusCode());
        log.info("Body = " + responseEntity.getBody());

        //데이터 변환
        return fromJSONtoItems(responseEntity.getBody());
    }

    public ItemDto postCall(String query) {

        //PathVariable 방식 이용
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/post-call/{query}")
                .encode()
                .build()
                .expand(query)
                .toUri();
        log.info("uri = " + uri);

        User user = new User("Robbie", "1234");
        //POST요청
        //restTemplate이 user를 자동으로 json으로 변환해줌
        ResponseEntity<ItemDto> responseEntity = restTemplate.postForEntity(uri, user, ItemDto.class);
        //응답코드 로그 찍어보기
        log.info("statusCode = " + responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    public List<ItemDto> exchangeCall(String token) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/exchange-call")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        //요청 바디로 보낼 객체 생성
        User user = new User("Robbie", "1234");

        RequestEntity<User> requestEntity = RequestEntity //요청 객체
                .post(uri) //post요청 생성
                .header("X-Authorization", token) //요청 헤더에 토큰 넣어주기
                .body(user); //바디에 User 객체 추가

        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        return fromJSONtoItems(responseEntity.getBody());
    }

    //json데이터 안에 items란 배열이 들어있고, items의 요소들이 json형태로 들어 있음 (중첩 json 형태)
//    {
//        "items":[
//        {"title":"Mac","price":3888000},
//        {"title":"iPad","price":1230000},
//        {"title":"iPhone","price":1550000},
//        {"title":"Watch","price":450000},
//        {"title":"AirPods","price":350000}
//	]
//    }
    //데이터 변환 메서드 ( JSON -> ItemDto  )
    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        //문자열을 JSONObject로 변환
        JSONObject jsonObject = new JSONObject(responseEntity);
        //키를 items로 설정 -> 키가 item인 배열을 찾아 이 요소들을 JSONArray로 꺼냄 (아래 부분)
        //        {"title":"Mac","price":3888000},
        //        {"title":"iPad","price":1230000},
        //        {"title":"iPhone","price":1550000},
        //        {"title":"Watch","price":450000},
        //        {"title":"AirPods","price":350000}

        //items[0] = {"title":"Mac","price":3888000}
        //items[1] = {"title":"iPad","price":1230000}
        //items[2] = {"title":"iPhone","price":1550000}
        //items[3] = {"title":"Watch","price":450000}
        //items[4] = {"title":"AirPods","price":350000}
        JSONArray items  = jsonObject.getJSONArray("items");
        List<ItemDto> itemDtoList = new ArrayList<>(); //변환 데이터를 리스트에 담기

        //{"title":"Mac","price":3888000 -> ItemDto(title="Mac", price=3888000) 변환
        for (Object item : items) {
            ItemDto itemDto = new ItemDto((JSONObject) item);
            itemDtoList.add(itemDto);
        }

        //최종 반환형태
//        List<ItemDto> = [
//        ItemDto(title="Mac", price=3888000),
//                ItemDto(title="iPad", price=1230000),
//                ItemDto(title="iPhone", price=1550000),
//                ItemDto(title="Watch", price=450000),
//                ItemDto(title="AirPods", price=350000)
//]
        return itemDtoList;
    }
}