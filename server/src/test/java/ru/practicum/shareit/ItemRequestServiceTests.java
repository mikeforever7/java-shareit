package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.InvalidUserRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceTests {

    private final ItemRequestServiceImpl itemRequestService;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;


    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("Test@Email.com");
        return userRepository.save(user);
    }

    private ItemRequest createRequest(User requester, String description) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        return itemRequestRepository.save(request);
    }

    private ItemRequestWithItemsDto createRequestDto(Long userId, String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(description);

        ItemRequestDto savedRequestDto = itemRequestService.addItemRequest(userId, dto);

        return itemRequestService.getItemRequest(savedRequestDto.getId());
    }

    @Test
    void shouldAddItemRequestSuccessfully() {
        User user = createUser();
        ItemRequestWithItemsDto savedRequestDto = createRequestDto(user.getId(), "Нужна книга");

        assertNotNull(savedRequestDto);
        assertEquals("Нужна книга", savedRequestDto.getDescription());
        assertTrue(savedRequestDto.getId() > 0);

        ItemRequest savedRequest = itemRequestRepository.findById(savedRequestDto.getId()).orElse(null);
        assertNotNull(savedRequest);
        assertEquals("Нужна книга", savedRequest.getDescription());
        assertNotNull(savedRequest.getCreated());
        assertNotNull(savedRequest.getRequester());
        assertEquals("Test User", savedRequest.getRequester().getName());
    }

    @Test
    void shouldGetUserItemRequestsSuccessfully() {
        User user = createUser();

        ItemRequest request = createRequest(user, "Нужна шапка");

        List<ItemRequestWithItemsDto> requests = itemRequestService.getUserItemRequests(user.getId());

        assertNotNull(requests);
        assertFalse(requests.isEmpty());
        assertEquals(1, requests.size());
        assertEquals("Нужна шапка", requests.getFirst().getDescription());
        assertNotNull(requests.getFirst().getCreated());
        assertNotNull(requests.getFirst().getRequesterId());
        assertEquals(user.getId(), requests.getFirst().getRequesterId());
        assertNotNull(requests.getFirst().getItems());
        assertTrue(requests.getFirst().getItems().isEmpty());
    }

    @Test
    void shouldGetAllItemRequestsSuccessfully() {
        User user1 = createUser();
        User user2 = new User();
        user2.setName("Test User2");
        user2.setEmail("Test2@email.com");
        User savedUser2 = userRepository.save(user2);

        createRequest(user1, "Нужна книга");
        createRequest(savedUser2, "Нужна шапка");

        List<ItemRequestDto> requests = itemRequestService.getAllItemRequests();

        assertNotNull(requests);
        assertFalse(requests.isEmpty());
        assertEquals(2, requests.size());
        assertEquals("Нужна книга", requests.get(1).getDescription());
        assertEquals("Нужна шапка", requests.get(0).getDescription());
    }

    @Test
    void shouldGetItemRequestSuccessfully() {
        User user = createUser();

        ItemRequest request = createRequest(user, "Нужна куртка");
        ItemRequestWithItemsDto requestDto = itemRequestService.getItemRequest(request.getId());
        assertNotNull(requestDto);
        assertEquals("Нужна куртка", requestDto.getDescription());
        assertNotNull(requestDto.getCreated());
        assertNotNull(requestDto.getRequesterId());
        assertEquals(user.getId(), requestDto.getRequesterId());
        assertNotNull(requestDto.getItems());
        assertTrue(requestDto.getItems().isEmpty());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getUserItemRequests(999L);
        });

        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRequestNotFound() {
        Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getItemRequest(999L);
        });

        assertEquals("Запрос с id=999 не найден", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidUserRequestExceptionWhenUserIdIsNull() {
        // Попытка добавить запрос с null userId
        assertThrows(InvalidUserRequestException.class, () -> {
            itemRequestService.addItemRequest(null, new ItemRequestDto());
        });
    }
}