package kr.hhplus.be.server.api.payment.controller;

import kr.hhplus.be.server.api.payment.service.PaymentService;
import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.ResponseUtil;
import kr.hhplus.be.server.domain.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 처리 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse> processPayment(@RequestBody PaymentDto.SetPayment.Request param) {
        paymentService.processPayment(param);
        return ResponseUtil.convertResponse();
    }

    /**
     * 결제 내역 조회 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getPaymentList(PaymentDto.GetPayment.Request param) {
        PaymentDto.GetPayment.Response response = paymentService.getPayment(param);
        return ResponseUtil.convertResponse(response);

    }


}