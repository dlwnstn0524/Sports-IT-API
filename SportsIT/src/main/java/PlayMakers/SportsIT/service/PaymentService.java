package PlayMakers.SportsIT.service;

import PlayMakers.SportsIT.domain.Competition;
import PlayMakers.SportsIT.domain.Member;
import PlayMakers.SportsIT.domain.Payment;
import PlayMakers.SportsIT.dto.PaymentDto;
import PlayMakers.SportsIT.enums.PaymentStatus;
import PlayMakers.SportsIT.enums.PaymentType;
import PlayMakers.SportsIT.repository.PaymentRepository;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Value("${oneport.apikey}")
    private String impKey;
    @Value("${oneport.secret}")
    private String impSecret;
    @Value("${oneport.imp.uid}")
    private String impUid;

    public PaymentDto.Response record(PaymentDto.PreRequest paymentDto) throws IOException, IllegalArgumentException{
        log.info("결제 내역 사전 등록");

        if(paymentDto.getImp_uid() == null) {
            String errMessage = "imp_uid가 null입니다.";
            log.error(errMessage);
            throw new IllegalArgumentException(errMessage);
        }
        if(!paymentDto.getImp_uid().equals(impUid)){
            String errMessage = "IMP_UID가 일치하지 않습니다.";
            log.error(errMessage);
            throw new IllegalArgumentException(errMessage);
        }

        // access token 받아오기
        String token = getToken();
        log.info("token : {}", token);

        paymentDto.setMerchant_uid(generateMerchatUid());

        // 요청 생성
        HttpEntity<PaymentDto.PreRequest> request = createOneportHttpRequestEntity(paymentDto, token);

        // POST 요청 전송 & 수신
        String url = "https://api.iamport.kr/payments/prepare";
        ResponseEntity<PaymentDto.Response> response = new RestTemplate().postForEntity(url, request, PaymentDto.Response.class);
        PaymentDto.Response paymentResponseBody = response.getBody();
        log.info("response : {}", paymentResponseBody);

        // 응답 확인
        int code = Objects.requireNonNull(paymentResponseBody).getCode();
        if (code == 1) {
            log.info("이미 등록된 결제 내역");
            throw new IOException("결제 내역 사전 등록 실패");
        }

        return paymentResponseBody;
    }

    public boolean validate(PaymentDto.PreRequest paymentDto) throws IOException {
        log.info("결제 내역 사후 검증");

        // access token 받아오기
        String token = getToken();
        log.info("token : {}", token);

        // 요청 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String url = "https://api.iamport.kr/payments/" + paymentDto.getImp_uid();
        log.info("요청 URL : {}", url);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();

        HashMap<String, Object> responseBody = new HashMap<>();
        // GET 요청 전송 & 수신
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<?> response = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, PaymentDto.PortOneResponse.class);

            responseBody.put("response", response.getBody());

            log.info("response : {}", responseBody);
        } catch (Exception e) {
            log.error("결제 내역 사후 검증 실패: {}", e.getMessage());
            return false;
        }


        // JSON 파싱
        log.info("responseType : {}", responseBody.get("response").getClass());
        PaymentDto.PortOneResponse portOneResponse = responseBody.get("response") instanceof PaymentDto.PortOneResponse ? (PaymentDto.PortOneResponse) responseBody.get("response") : null;
        if(portOneResponse == null){
            log.error("결제 내역 사후 검증 실패: {}", "결제 내역이 존재하지 않습니다.");
            return false;
        }

        // 필요한 속성 추출
        String expected_imp_uid = paymentDto.getImp_uid();
        String expected_merchant_uid = portOneResponse.getResponse().getMerchant_uid();
        Long expected_amount = portOneResponse.getResponse().getAmount();

        // 검증
        return !isTampered(paymentDto, expected_imp_uid, expected_merchant_uid, expected_amount);
    }

    public Payment createOrder(PaymentDto.Request requestDto, Member member, Competition competition){
        log.info("결제 생성");
        log.info(requestDto.toString());

        PaymentStatus status = requestDto.getStatusEnum();
        PaymentType type = requestDto.getPaymentTypeEnum();

        Payment newPayment = Payment.builder()
                .impUid(requestDto.getImp_uid())
                .merchantUid(requestDto.getMerchant_uid())
                .amount(requestDto.getAmount())
                .type(type)
                .content(requestDto.getContent())
                .status(status)
                .buyer(member)
                .competition(competition)
                .build();

        paymentRepository.save(newPayment);

        return newPayment;
    }
    public List<PaymentDto.Detail> getPaymentsByBuyer(Member buyer){
        List<Payment> payments = paymentRepository.findByBuyer(buyer);
        List<PaymentDto.Detail> paymentDtos = new ArrayList<>();

        for(Payment payment : payments){
            paymentDtos.add(PaymentDto.Detail.builder()
                    .imp_uid(payment.getImpUid())
                    .merchant_uid(payment.getMerchantUid())
                    .amount(payment.getAmount())
                    .paymentType(payment.getType())
                    .content(payment.getContent())
                    .status(payment.getStatus())
                    .build());
        }

        return paymentDtos;
    }

    private static boolean isTampered(PaymentDto.PreRequest paymentDto, String expected_imp_uid, String expected_merchant_uid, Long expected_amount) {
        if(paymentDto.getImp_uid().equals(expected_imp_uid)
                && paymentDto.getMerchant_uid().equals(expected_merchant_uid)
                && paymentDto.getAmount().equals(expected_amount)){
            log.info("결제 내역 검증 성공");
            return false;
        } else {
            log.info("결제 내역 검증 실패");
            return true;
        }
    }

    @NotNull
    private static HttpEntity<PaymentDto.PreRequest> createOneportHttpRequestEntity(PaymentDto.PreRequest paymentDto, String token) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        // 요청 생성
        HttpEntity<PaymentDto.PreRequest> request = new HttpEntity<>(paymentDto, headers);
        return request;
    }

    private String getToken() throws IOException {
        /*
        * OnePort에 access token을 요청하는 API
         */

        URL url = new URL("https://api.iamport.kr/users/getToken");

        HttpsURLConnection conn = setHttpsURLConnection(url);
        JsonObject json = new JsonObject();

        json.addProperty("imp_key", impKey);
        json.addProperty("imp_secret", impSecret);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));

        bw.write(json.toString());
        bw.flush();
        bw.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

        Gson gson = new Gson();

        String response = gson.fromJson(br.readLine(), Map.class).get("response").toString();
        String token = gson.fromJson(response, Map.class).get("access_token").toString();

        log.info("response : {}", response);

        br.close();
        conn.disconnect();

        return "Bearer " + token;
    }

    @NotNull
    private static HttpsURLConnection setHttpsURLConnection(URL url) throws IOException {
        HttpsURLConnection conn = null;
        conn = (HttpsURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    private String generateMerchatUid() {
        /*
        * merchant_uid 생성
        * */
        String merchantUid = "PAY" + UUID.randomUUID().toString().replaceAll("-", "");
        if(paymentRepository.existsById(merchantUid)){
            return generateMerchatUid();
        }
        return "PAY"+UUID.randomUUID().toString().replaceAll("-", "");
    }

    public List<PaymentDto.Detail> findAll(){
        List<PaymentDto.Detail> paymentDtos = new ArrayList<>();
        for(Payment payment : paymentRepository.findAll()) {
            paymentDtos.add(PaymentDto.Detail.builder()
                    .imp_uid(payment.getImpUid())
                    .merchant_uid(payment.getMerchantUid())
                    .amount(payment.getAmount())
                    .paymentType(payment.getType())
                    .content(payment.getContent())
                    .status(payment.getStatus())
                    .build());
        }
        return paymentDtos;
    }
}
