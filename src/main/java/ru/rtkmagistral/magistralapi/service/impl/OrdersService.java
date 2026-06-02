package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.minio.MinioDTO;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;
import ru.rtkmagistral.magistralapi.dto.order.*;
import ru.rtkmagistral.magistralapi.dto.user.UserBlock;
import ru.rtkmagistral.magistralapi.exception.OrderException;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.mapper.IOrderMapper;
import ru.rtkmagistral.magistralapi.mapper.IUserMapper;
import ru.rtkmagistral.magistralapi.repository.IOrderRepository;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с сущностью "Заказ на доставку".
 */
@Service
@RequiredArgsConstructor
public class OrdersService implements IOrdersService {
    private final UserRepository userRepository;
    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;
    private final IUserMapper userMapper;

    private final IIdempotencyKeyService idempotencyKeyService;
    private final IOrderApplicationDocxGeneratorService docxGeneratorService;
    private final IMessageService messageService;

    private final IPriceQuoteService priceQuoteService;

    @Value("${mail.document.contract-text}")
    private String contractText;

    @Value("${docx.templates.order-application}")
    private String orderApplication;

    /**
     * Метод для создания заказа на доставку в системе.
     *
     * @param createOrderRequest все необходимые данные для создания заказа на доставку в системе.
     * @param email              адрес электронной почты пользователя, для нахождения пользователя, к которому следует прикрепить созданный заказ.
     * @return результат создания заказа на доставку.
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserException("USER_NOT_FOUND"));

        Order order = orderMapper.toEntity(createOrderRequest);
        order.setOrderStatus(Order.OrderStatus.ACCEPTED);
        order.setUser(user);
        order.setPriceInKopeika(calculatePrice(createOrderRequest));

        orderRepository.save(order);

        DocumentMailRequest documentMailRequest = formMailRequest(user, order);
        MinioDTO minioDTO = new MinioDTO(documentMailRequest.getDocument(), documentMailRequest.getFilename());

        messageService.sendMinioMessageToQueue(minioDTO);
        messageService.sendDocumentMessageToQueue(documentMailRequest);

        return new OrderResponse("CREATED", true, orderRepository.countOrdersByUser(user), order.getPriceInKopeika());
    }

    /**
     * Метод для идемпотентного создания заказа на доставку в системе.
     *
     * @param id                ключ идемпотентности.
     * @param email              адрес электронной почты пользователя, для нахождения пользователя, к которому следует прикрепить созданный заказ.
     * @param createOrderRequest все необходимые данные для создания заказа на доставку в системе.
     * @param method             http-метод запроса.
     * @param path               URI запроса.
     * @return результат создания заказа на доставку.
     */
    @Override
    @Transactional
    public OrderResponseDTO createIdempotentOrder(UUID id, String email, CreateOrderRequest createOrderRequest, String method, String path) {
        Optional<IdempotencyKey> idempotencyKeyOptional = idempotencyKeyService.readIdempotencyKey(id);

        if (idempotencyKeyOptional.isPresent()) {
            IdempotencyKey idempotencyKey = idempotencyKeyOptional.get();

            if (idempotencyKey.getIdempotencyKeyStatus().equals(IdempotencyKey.IdempotencyKeyStatus.IN_PROGRESS)) {
                throw new OrderException("ORDER_IS_STILL_BEING_CREATED");
            }

            HttpHeaders headers = new HttpHeaders();
            idempotencyKey.getResponseHeaders().forEach(headers::add);

            return new OrderResponseDTO(
                    idempotencyKey.getResponseStatus(),
                    headers,
                    idempotencyKey.getResponseBody()
            );
        }

        IdempotencyKeyDTO idempotencyKeyDTO = new IdempotencyKeyDTO(
                id,
                method,
                path,
                createOrderRequest.toString()
        );

        idempotencyKeyService.createIdempotencyKey(idempotencyKeyDTO);

        OrderResponse orderResponse = createOrder(createOrderRequest, email);

        idempotencyKeyService.deactivateIdempotencyKey(id, 201, new HashMap<>(), orderResponse);

        return new OrderResponseDTO(
                201,
                new HttpHeaders(),
                orderResponse
        );
    }

    /**
     * Рассчитывает стоимость заказа по правилам экспресс-отправлений. Присланная клиентом цена
     * не используется — стоимость считается заново через {@link IPriceQuoteService}.
     *
     * @param request данные создаваемого заказа.
     * @return итоговая цена отправления в копейках.
     */
    private long calculatePrice(CreateOrderRequest request) {
        return priceQuoteService.calculatePriceInKopeika(
                request.getShippingAddress(),
                request.getArrivalAddress(),
                request.getShippingFromOffice(),
                request.getArrivalToOffice(),
                request.getWeightGr(),
                request.getLengthCentiCm(),
                request.getWidthCentiCm(),
                request.getHeightCentiCm(),
                request.getNatureOfInvestment(),
                request.getCostOfInvestmentInKopeika()
        );
    }

    private DocumentMailRequest formMailRequest(User user, Order order) {
        UserBlock userBlock = userMapper.toDto(user);
        OrderBlock orderBlock = orderMapper.toDto(order);

        OrderDocumentDTO docDto = new OrderDocumentDTO(
                order.getId().toString(),
                OffsetDateTime.now(),
                contractText,
                OffsetDateTime.now(),
                order.getWishingDeliveryTime(),
                1,
                order.getComment(),
                order.getPriceInKopeika(),
                userBlock,
                orderBlock
        );

        byte[] docx;
        try (InputStream template = new ClassPathResource(orderApplication).getInputStream()) {
            docx = docxGeneratorService.generate(template, docDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String suffix = userBlock.getCompanyName() == null ?
                userBlock.getName() + userBlock.getSurname() :
                userBlock.getCompanyName().replace('\"', '\'');

        String filename = "Заявка-" + suffix + "-" + order.getId() + ".docx";
        String subject = "Заявка № " + order.getId();

        return new DocumentMailRequest(subject, docx, filename);
    }
}
