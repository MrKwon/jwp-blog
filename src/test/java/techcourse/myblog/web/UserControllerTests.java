package techcourse.myblog.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;

import techcourse.myblog.domain.UserRepository;
import techcourse.myblog.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
class UserControllerTests extends WebClientGenerator {

    @Autowired
    private UserRepository userRepository;

    private String name;
    private String email;
    private String password;
    private

    @BeforeEach
    void setup() {
        name = "name";
        email = "email@email";
        password = "password";
    }

    @Test
    void 로그인_페이지_요청() {
        response(GET, "/login")
                .expectStatus()
                .isOk();
    }

    @Test
    void 회원목록_페이지_요청() {
        response(GET, "/users")
                .expectStatus()
                .isOk();;
    }

    @Test
    void 회원가입_페이지_요청() {
        response(GET, "/signup")
                .expectStatus()
                .isOk();
    }

    @Test
    void 로그아웃() {
        response(GET, "/logout")
                .expectStatus()
                .isFound();
    }

    @Test
    void 비로그인시_회원_정보_페이지_요청_불가() {
        response(GET, "/mypage")
                .expectStatus()
                .isFound();
    }

    @Test
    void 비로그인시_회원_정보_수정_페이지_요청_불가() {
        response(GET, "/mypage/edit")
                .expectStatus()
                .isFound();
    }

    @Test
    void 회원가입_성공() {
        UserDto userDto = new UserDto(name, email, password);

        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    assertEquals(user.getEmail(), userDto.getEmail());
                    assertEquals(user.getName(), userDto.getName());
                    assertEquals(user.getPassword(), userDto.getPassword());
                });
    }

    @Test
    void 이름_길이_규칙_위반() {
        name = "k";
        UserDto userDto = new UserDto(name, email, password);

        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(res -> assertBodyContainsTrue(res, "이름은 2~10자, 숫자나 특수문자가 포함될 수 없습니다."));

        assertFalse(userRepository.findByEmail(email).isPresent());
    }

    @Test
    void 이메일_형식_위반() {
        email = "email";
        UserDto userDto = new UserDto(name, email, password);

        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(res -> assertBodyContainsTrue(res, "이메일 양식을 지켜주세요."));

        assertFalse(userRepository.findByEmail(email).isPresent());
    }

    private void assertBodyContainsTrue(EntityExchangeResult<byte[]> res, String bodyContents) {
        String body = new String(Objects.requireNonNull(res.getResponseBody()));
        assertThat(body.contains(bodyContents)).isTrue();
    }

    @Test
    void 비밀번호_길이_규칙_위반() {
        password = "1234567";
        UserDto userDto = new UserDto(name, email, password);

        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(res -> assertBodyContainsTrue(res, "비밀번호는 8자 이상, 소문자, 대문자, 숫자, 특수문자의 조합으로 입력하세요."));

        assertFalse(userRepository.findByEmail(email).isPresent());
    }

    @Test
    void 동일_이메일_중복_가입() {
        email = "registered1@email";
        UserDto userDto = new UserDto(name, email, password);

        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(res -> assertBodyContainsTrue(res, "이미 존재하는 email입니다."));

        assertTrue(userRepository.findByEmail(email).isPresent());
    }

    @Test
    void 회원목록_페이지() {
        response(GET, "/users")
                .expectStatus()
                .isOk();
    }

    @Test
    void 로그인_성공_시_메인_화면으로_리다이렉트() {
        final String forLoginEmail = "registered2@email";

        UserDto userDto = new UserDto(name, forLoginEmail, password);
        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        response(POST, "/login", parser(userDto))
                .expectStatus()
                .isFound();;
    }

    @Test
    void 해당_이메일이_없는_경우_에러() {
        final String notSignedUpEmail = "thisisfake@login.com";

        UserDto userDto = new UserDto(name, notSignedUpEmail, password);

        response(POST, "/login", parser(userDto))
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(res -> assertBodyContainsTrue(res, "이메일을 확인해주세요."));
    }

    @Test
    void 비밀번호_불일치하는_경우_에러() {
        final String forLoginEmail = "registered3@email";

        UserDto userDto = new UserDto(name, forLoginEmail, password);
        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        UserDto userDtoWrongPassword = new UserDto(name, forLoginEmail, "abcdeghf");
        response(POST, "/login", parser(userDtoWrongPassword))
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(res -> assertBodyContainsTrue(res, "비밀번호를 확인해주세요."));
    }

    @Test
    void 로그인_상태에서_회원가입_요청시_리다이렉트() {
        UserDto userDto = new UserDto("루피", "luffy1@pirates.com", "12345678");
        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        loggedInAndRequest(userDto, "/signup")
                .isFound();
    }

    @Test
    void 로그인_상태에서_로그인_페이지_요청시_리다이렉트() {
        UserDto userDto = new UserDto("루피", "luffy2@pirates.com", "12345678");
        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        loggedInAndRequest(userDto, "/login")
                .isFound();
    }

    @Test
    void 로그인_상태에서_마이페이지_요청시_성공() {
        UserDto userDto = new UserDto("루피", "luffy3@pirates.com", "12345678");
        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        loggedInAndRequest(userDto, "/mypage")
                .isOk();
    }

    @Test
    void 로그인_상태에서_정보수정페이지_요청시_성공() {
        UserDto userDto = new UserDto("루피", "luffy4@pirates.com", "12345678");
        response(POST, "/users", parser(userDto))
                .expectStatus()
                .isFound();

        loggedInAndRequest(userDto, "/mypage/edit")
                .isOk();
    }

    @AfterEach
    void 초기화() {
        userRepository.deleteAll();
    }

    private StatusAssertions loggedInAndRequest(UserDto userDto, String path) {
        MultiValueMap<String, ResponseCookie> cookies
                = response(POST, "/login", parser(userDto))
                .expectStatus()
                .isFound()
                .returnResult(Void.class)
                .getResponseCookies();

        return requestCookie(GET, path, new LinkedMultiValueMap<>())
                .cookie("JSESSIONID", Objects.requireNonNull(cookies.getFirst("JSESSIONID")).getValue())
                .exchange()
                .expectStatus();
    }

    private MultiValueMap<String, String> parser(UserDto userDto) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("email", userDto.getEmail());
        multiValueMap.add("name", userDto.getName());
        multiValueMap.add("password", userDto.getPassword());
        return multiValueMap;
    }
}