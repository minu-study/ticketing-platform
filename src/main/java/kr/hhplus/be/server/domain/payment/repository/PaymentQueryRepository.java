package kr.hhplus.be.server.domain.payment.repository;

import kr.hhplus.be.server.domain.payment.dto.PaymentDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface PaymentQueryRepository {

    List<PaymentDto.PaymentView> getPaymentList(UUID userId);
}