package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.dto.suitcase.CreateSuitcaseRequest;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;
import ru.rtkmagistral.magistralapi.service.spec.ISuitcaseService;

import java.util.UUID;

/**
 * Сервис формы «Чемоданы». Получателем доставки всегда выступает Магистраль, поэтому его
 * реквизиты и значения по умолчанию (вид и характер вложения, отсутствующие в форме) берутся
 * из конфигурации. Из данных формы и этих реквизитов собирается обычный {@link CreateOrderRequest},
 * после чего заявка обрабатывается общим потоком создания заказа.
 */
@Service
@RequiredArgsConstructor
public class SuitcaseService implements ISuitcaseService {
    private final IOrdersService ordersService;

    @Value("${suitcase.receiver.fio}")
    private String receiverFio;

    @Value("${suitcase.receiver.phone}")
    private String receiverPhone;

    @Value("${suitcase.defaults.type-of-shipment}")
    private Order.TypeOfShipment defaultTypeOfShipment;

    @Value("${suitcase.defaults.nature-of-investment}")
    private NatureOfInvestment defaultNatureOfInvestment;

    @Value("${suitcase.price-coefficient}")
    private double priceCoefficient;

    @Override
    public OrderResponseDTO createIdempotentSuitcase(UUID id,
                                                     String email,
                                                     CreateSuitcaseRequest createSuitcaseRequest,
                                                     String method,
                                                     String path) {
        CreateOrderRequest createOrderRequest = toOrderRequest(createSuitcaseRequest);
        return ordersService.createIdempotentOrder(id, email, createOrderRequest, method, path, priceCoefficient);
    }

    /**
     * Преобразует заявку на чемодан в заявку на заказ: поля чемодана (включая адреса забора и
     * доставки) переносятся напрямую, контакты получателя берутся из конфигурации Магистрали,
     * а вид и характер вложения подставляются значениями по умолчанию.
     *
     * @param request данные формы «Чемоданы».
     * @return заявка на заказ, готовая к обработке общим потоком.
     */
    private CreateOrderRequest toOrderRequest(CreateSuitcaseRequest request) {
        return new CreateOrderRequest(
                request.getShippingAddress(),
                request.getShippingFromOffice(),
                request.getArrivalAddress(),
                request.getArrivalToOffice(),
                request.getLengthCentiCm(),
                request.getWidthCentiCm(),
                request.getHeightCentiCm(),
                request.getWeightGr(),
                request.getCostOfInvestmentInKopeika(),
                defaultTypeOfShipment,
                defaultNatureOfInvestment,
                request.getComment(),
                request.getDeliverAsSoonAsPossible(),
                request.getWishingDeliveryTime(),
                receiverFio,
                receiverPhone,
                request.getAgreeWithTheTermsOfTheAgreement()
        );
    }
}
