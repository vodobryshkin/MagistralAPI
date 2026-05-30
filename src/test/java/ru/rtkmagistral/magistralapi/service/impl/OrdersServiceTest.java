package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.minio.MinioDTO;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderBlock;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserBlock;
import ru.rtkmagistral.magistralapi.exception.OrderException;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.mapper.IOrderMapper;
import ru.rtkmagistral.magistralapi.mapper.IUserMapper;
import ru.rtkmagistral.magistralapi.repository.IOrderRepository;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.IIdempotencyKeyService;
import ru.rtkmagistral.magistralapi.service.spec.IMessageService;
import ru.rtkmagistral.magistralapi.service.spec.IOrderApplicationDocxGeneratorService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    IOrderRepository orderRepository;
    @Mock
    IOrderMapper orderMapper;
    @Mock
    IUserMapper userMapper;
    @Mock
    IIdempotencyKeyService idempotencyKeyService;
    @Mock
    IOrderApplicationDocxGeneratorService docxGeneratorService;
    @Mock
    IMessageService messageService;

    @InjectMocks
    OrdersService ordersService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ordersService, "contractText", "№15930э");
        ReflectionTestUtils.setField(ordersService, "orderApplication", "templates/заявка.docx");
    }

    private User physicalUser() {
        User user = new User("Владимир", "Добрышкин", "Александрович",
                "vova@example.com", "+79614667210",
                "h".getBytes(StandardCharsets.UTF_8));
        user.setUserType(User.UserType.INDIVIDUAL);
        return user;
    }

    private CreateOrderRequest createOrderRequest() {
        return new CreateOrderRequest(
                "г. Москва, ул. Тверская, д. 1",
                "г. Москва, ул. Арбат, д. 10",
                10, 10, 10,
                100,
                10000L,
                Order.TypeOfShipment.PACKAGE,
                NatureOfInvestment.HOUSEHOLD_CHEMICALS,
                "Комментарий к заказу",
                true,
                null,
                500L,
                "Петров Пётр",
                "+79991112233",
                true
        );
    }

    @Test
    @DisplayName("createOrder сохраняет заказ, ставит статус ACCEPTED и публикует сообщения")
    void createOrder_savesOrderAndPublishesMessages() throws Exception {
        CreateOrderRequest req = createOrderRequest();
        User user = physicalUser();

        when(userRepository.findUserByEmail("vova@example.com")).thenReturn(Optional.of(user));

        Order order = new Order();
        order.setId(42L);
        when(orderMapper.toEntity(req)).thenReturn(order);
        when(orderRepository.countOrdersByUser(user)).thenReturn(1L);

        UserBlock userBlock = new UserBlock();
        userBlock.setName(user.getName());
        userBlock.setSurname(user.getSurname());
        userBlock.setUserType(User.UserType.INDIVIDUAL);
        when(userMapper.toDto(user)).thenReturn(userBlock);

        OrderBlock orderBlock = new OrderBlock();
        when(orderMapper.toDto(order)).thenReturn(orderBlock);

        when(docxGeneratorService.generate(any(), any())).thenReturn(new byte[]{1, 2, 3});

        OrderResponse response = ordersService.createOrder(req, "vova@example.com");

        assertThat(response.getMessage()).isEqualTo("CREATED");
        assertThat(response.isStatus()).isTrue();
        assertThat(response.getAmountOfOrders()).isEqualTo(1L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderStatus()).isEqualTo(Order.OrderStatus.ACCEPTED);
        assertThat(orderCaptor.getValue().getUser()).isSameAs(user);

        ArgumentCaptor<DocumentMailRequest> mailCaptor =
                ArgumentCaptor.forClass(DocumentMailRequest.class);
        verify(messageService).sendDocumentMessageToQueue(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getFilename())
                .startsWith("Заявка-")
                .endsWith("-42.docx");
        assertThat(mailCaptor.getValue().getSubject()).isEqualTo("Заявка № 42");

        verify(messageService).sendMinioMessageToQueue(any(MinioDTO.class));
    }

    @Test
    @DisplayName("createOrder использует название компании в имени файла для юридического лица")
    void createOrder_legalEntity_filenameUsesCompanyName() throws Exception {
        CreateOrderRequest req = createOrderRequest();
        User user = physicalUser();
        user.setUserType(User.UserType.BUSINESS);

        when(userRepository.findUserByEmail("vova@example.com")).thenReturn(Optional.of(user));

        Order order = new Order();
        order.setId(7L);
        when(orderMapper.toEntity(req)).thenReturn(order);
        when(orderRepository.countOrdersByUser(user)).thenReturn(2L);

        UserBlock userBlock = new UserBlock();
        userBlock.setCompanyName("ООО \"Магистраль\"");
        userBlock.setUserType(User.UserType.BUSINESS);
        when(userMapper.toDto(user)).thenReturn(userBlock);

        OrderBlock orderBlock = new OrderBlock();
        when(orderMapper.toDto(order)).thenReturn(orderBlock);
        when(docxGeneratorService.generate(any(), any())).thenReturn(new byte[]{1});

        ordersService.createOrder(req, "vova@example.com");

        ArgumentCaptor<DocumentMailRequest> captor = ArgumentCaptor.forClass(DocumentMailRequest.class);
        verify(messageService).sendDocumentMessageToQueue(captor.capture());
        assertThat(captor.getValue().getFilename())
                .doesNotContain("\"")
                .contains("ООО 'Магистраль'");
    }

    @Test
    @DisplayName("createOrder кидает USER_NOT_FOUND если пользователя нет")
    void createOrder_unknownUser_throws() {
        when(userRepository.findUserByEmail("nope@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordersService.createOrder(createOrderRequest(), "nope@example.com"))
                .isInstanceOf(UserException.class)
                .hasMessage("USER_NOT_FOUND");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createIdempotentOrder при IN_PROGRESS-ключе кидает ORDER_IS_STILL_BEING_CREATED")
    void createIdempotentOrder_inProgress_throws() {
        UUID id = UUID.randomUUID();
        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKeyStatus(IdempotencyKey.IdempotencyKeyStatus.IN_PROGRESS);
        when(idempotencyKeyService.readIdempotencyKey(id)).thenReturn(Optional.of(key));

        assertThatThrownBy(() -> ordersService.createIdempotentOrder(
                id, "vova@example.com", createOrderRequest(), "POST", "/api/v1/orders"))
                .isInstanceOf(OrderException.class)
                .hasMessage("ORDER_IS_STILL_BEING_CREATED");
    }

    @Test
    @DisplayName("createIdempotentOrder возвращает кэшированный ответ при COMPLETED ключе")
    void createIdempotentOrder_completed_returnsCached() {
        UUID id = UUID.randomUUID();
        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKeyStatus(IdempotencyKey.IdempotencyKeyStatus.COMPLETED);
        key.setResponseStatus(201);
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Cached", "1");
        key.setResponseHeaders(headers);
        OrderResponse cached = new OrderResponse("CREATED", true, 5L);
        key.setResponseBody(cached);

        when(idempotencyKeyService.readIdempotencyKey(id)).thenReturn(Optional.of(key));

        OrderResponseDTO result = ordersService.createIdempotentOrder(
                id, "vova@example.com", createOrderRequest(), "POST", "/api/v1/orders");

        assertThat(result.getCode()).isEqualTo(201);
        assertThat(result.getBody()).isEqualTo(cached);
        assertThat(result.getHeaders().getFirst("X-Cached")).isEqualTo("1");

        verify(userRepository, never()).findUserByEmail(any());
    }

    @Test
    @DisplayName("createIdempotentOrder для нового ключа создаёт его, выполняет заказ и деактивирует ключ")
    void createIdempotentOrder_newKey_createsOrderAndDeactivatesKey() throws Exception {
        UUID id = UUID.randomUUID();
        when(idempotencyKeyService.readIdempotencyKey(id)).thenReturn(Optional.empty());

        CreateOrderRequest req = createOrderRequest();
        User user = physicalUser();
        when(userRepository.findUserByEmail("vova@example.com")).thenReturn(Optional.of(user));

        Order order = new Order();
        order.setId(99L);
        when(orderMapper.toEntity(req)).thenReturn(order);
        when(orderRepository.countOrdersByUser(user)).thenReturn(3L);

        UserBlock userBlock = new UserBlock();
        userBlock.setName(user.getName());
        userBlock.setSurname(user.getSurname());
        when(userMapper.toDto(user)).thenReturn(userBlock);
        when(orderMapper.toDto(order)).thenReturn(new OrderBlock());
        when(docxGeneratorService.generate(any(), any())).thenReturn(new byte[]{42});

        OrderResponseDTO result = ordersService.createIdempotentOrder(
                id, "vova@example.com", req, "POST", "/api/v1/orders");

        assertThat(result.getCode()).isEqualTo(201);
        assertThat(result.getBody().getMessage()).isEqualTo("CREATED");
        assertThat(result.getHeaders()).isEqualTo(new HttpHeaders());

        verify(idempotencyKeyService).createIdempotencyKey(any());
        verify(idempotencyKeyService).deactivateIdempotencyKey(
                org.mockito.ArgumentMatchers.eq(id),
                org.mockito.ArgumentMatchers.eq(201),
                any(),
                org.mockito.ArgumentMatchers.eq(result.getBody())
        );
    }
}
