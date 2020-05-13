package com.keepreal.madagascar.baobob.loginExecutor;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.loginExecutor.model.IOSLoginInfo;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.Objects;

/**
 * Represents a login executor working with IOS jwt.
 */
public class JWTIOSLoginExecutor implements LoginExecutor {

    private final GrpcResponseUtils grpcResponseUtils;
    private final UserService userService;
    private final LocalTokenGranter tokenGranter;

    /**
     * Constructs this executor
     *
     * @param userService   {@link UserService}.
     * @param tokenGranter  Token granter.
     */
    public JWTIOSLoginExecutor(UserService userService, LocalTokenGranter tokenGranter) {
        this.grpcResponseUtils = new GrpcResponseUtils();
        this.userService = userService;
        this.tokenGranter = tokenGranter;
    }

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasJwtIsoLoginPayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }
        return this.loginIOS(loginRequest.getJwtIsoLoginPayload().getIdentifyToken())
                .flatMap(this::retrieveOrCreateUserByUnionId)
                .map(this.tokenGranter::grant)
                .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
    }

    /**
     * Retrieves a user by union id, create a new one if not exists.
     *
     * @param iosLoginInfo {@link IOSLoginInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> retrieveOrCreateUserByUnionId(IOSLoginInfo iosLoginInfo) {
        return this.userService.retrieveUserByUnionIdMono(iosLoginInfo.getUnionId())
                .switchIfEmpty(this.createNewUserFromIOS(iosLoginInfo));
    }

    /**
     * Creates a new user from ios info.
     *
     * @param iosLoginInfo {@link IOSLoginInfo}
     * @return {@link UserMessage}
     */
    private Mono<UserMessage> createNewUserFromIOS(IOSLoginInfo iosLoginInfo) {
        return this.userService.createUserByIOSUserInfoMono(iosLoginInfo);
    }

    /**
     * Logs in ios with jwt.
     *
     * @param jwt from IOS client
     * @return {@link IOSLoginInfo}
     */
    private Mono<IOSLoginInfo> loginIOS(String jwt) {
        if (jwt.split("\\.").length > 1) {
            String claim = new String(Base64.decodeBase64(jwt.split("\\.")[1]));
            String aud = JSONObject.parseObject(claim).get("aud").toString();
            String sub = JSONObject.parseObject(claim).get("sub").toString();
            PublicKey publicKey = generatorPublicKey();
            if (!verify(publicKey, jwt, aud, sub)) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID);
            }
            return Mono.just(new IOSLoginInfo());
        }
        throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
    }

    /**
     * Requests apple to get message.
     *
     * @return {@link PublicKey}
     */
    private PublicKey generatorPublicKey() {
        Mono<Jwk> keys = WebClient.create("https://appleid.apple.com/auth/keys")
                .get()
                .retrieve()
                .bodyToMono(JSONObject.class)
                .map(j -> j.getString("keys"))
                .map(JSONObject::parseArray)
                .map(array -> JSONObject.parseObject(array.getString(0)))
                .map(Jwk::fromValues);
        try {
            return Objects.requireNonNull(keys.block()).getPublicKey();
        } catch (InvalidPublicKeyException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

    /**
     * Verifies jwt validity.
     *
     * @param key       {@link PublicKey}
     * @param jwt       from IOS client
     * @param audience  receiver App Id (parse from jwt)
     * @param subject   unique Id (parse from jwt)
     * @return if jwt valid
     */
    private boolean verify(PublicKey key, String jwt, String audience, String subject) {
        JwtParser jwtParser = Jwts.parser().setSigningKey(key);
        jwtParser.requireIssuer("https://appleid.apple.com");
        jwtParser.requireAudience(audience);
        jwtParser.requireSubject(subject);
        try {
            Jws<Claims> claim = jwtParser.parseClaimsJws(jwt);
            return claim != null && claim.getBody().containsKey("auth_time");
        } catch (ExpiredJwtException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID);
        }
    }

}
