package ru.practicum.shareit;

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.InvalidUserRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
public class ItemServiceTests {
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final EntityManager entityManager;

    private Long ownerId;
    private Long itemId;
    private Item savedItem;
    private final Long invalidId = 999L;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("Test Owner");
        owner.setEmail("test@Email.com");
        User savedOwner = userRepository.save(owner);
        ownerId = savedOwner.getId();

        Item item = new Item();
        item.setOwner(savedOwner);
        item.setName("Test Item");
        item.setDescription("Test Item Description");
        item.setAvailable(true);
        savedItem = itemRepository.save(item);
        itemId = savedItem.getId();
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ShouldThrowNotFoundException() {
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(ownerId, invalidId));
        assertThat(thrown.getMessage(), equalTo("Вещь с id =" + invalidId + " не найдена"));
    }

    @Test
    void getItemById_WhenUserIsOwner_ShouldReturnItemWithCommentsAndBookings() {
        User booker1 = createDefaultUser();
        booker1 = userRepository.save(booker1);

        User booker2 = createDefaultUser();
        booker2.setEmail("Some@email.com");
        booker2 = userRepository.save(booker2);

        Booking lastBooking = createDefaultBooking(savedItem, booker1);
        lastBooking.setStart(LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        lastBooking.setEnd(LocalDateTime.of(2024, 1, 1, 1, 1, 1));

        lastBooking = bookingRepository.save(lastBooking);

        CommentDto commentDto = createDefaultCommentDto();
        commentDto = itemService.addNewComment(booker1.getId(), itemId, commentDto);
        entityManager.clear();
        Booking nextBooking = createDefaultBooking(savedItem, booker2);
        nextBooking.setStart(LocalDateTime.of(2026, 1, 1, 1, 1, 1));
        nextBooking.setEnd(LocalDateTime.of(2026, 2, 1, 1, 1, 1));
        nextBooking = bookingRepository.save(nextBooking);
        ItemWithCommentsDto receivedItem = assertDoesNotThrow(() -> itemService.getItemById(ownerId, itemId));

        assertThat(receivedItem.getId(), equalTo(itemId));
        assertThat(receivedItem.getLastBooking().getStart(), equalTo(lastBooking.getStart()));
        assertThat(receivedItem.getNextBooking().getStart(), equalTo(nextBooking.getStart()));
        assertThat(receivedItem.getComments().getFirst().getText(), equalTo(commentDto.getText()));
        assertThat(receivedItem.getName(), equalTo(savedItem.getName()));
        assertThat(receivedItem.getDescription(), equalTo(savedItem.getDescription()));
        assertThat(receivedItem.getAvailable(), equalTo(savedItem.getAvailable()));
        assertThat(receivedItem.getOwnerId(), equalTo(ownerId));
    }

    @Test
    void getItemById_WhenUserIsNotOwner_ShouldReturnItemWithoutBookings() {

        User booker = createDefaultUser();

        User savedBooker = userRepository.save(booker);

        Booking booking = createDefaultBooking(savedItem, booker);
        bookingRepository.save(booking);

        ItemWithCommentsDto receivedItem = assertDoesNotThrow(() -> itemService.getItemById(savedBooker.getId(), itemId));
        assertThat(receivedItem.getId(), equalTo(itemId));
        assertThat(receivedItem.getLastBooking(), nullValue());
        assertThat(receivedItem.getNextBooking(), nullValue());
    }

    @Test
    void getItems_WhenUserHasItemsAndOtherUsersExist_ShouldReturnOnlyUsersItems() {
        ItemDto newItemDto = createDefaultItemDto();
        itemService.addNewItem(ownerId, newItemDto);
        User otherUser = createDefaultUser();
        userRepository.save(otherUser);
        ItemDto itemForOtherUser = createDefaultItemDto();
        itemService.addNewItem(otherUser.getId(), itemForOtherUser);

        List<ItemDto> itemsForOwner = itemService.getItems(ownerId);
        assertThat(itemsForOwner.size(), equalTo(2));
        for (ItemDto itemDto : itemsForOwner) {
            assertThat(itemDto.getOwnerId(), equalTo(ownerId));
        }
        assertThat(itemsForOwner, hasItem(hasProperty("id", equalTo(itemId))));
        assertThat(itemsForOwner, hasItem(hasProperty("name", equalTo(newItemDto.getName()))));
    }

    @Test
    void addItem_WhenOwnerNotExists_ShouldThrowNotFoundException() {
        ItemDto itemDto = createDefaultItemDto();
        assertThrows(NotFoundException.class, () -> itemService.addNewItem(2L, itemDto));
    }

    @Test
    void addItem_WhenUserIdIsNull_ShouldThrowInvalidUserRequestException() {
        ItemDto itemDto = createDefaultItemDto();
        assertThrows(InvalidUserRequestException.class, () -> itemService.addNewItem(null, itemDto));
    }

    @Test
    void addItem_WhenRequestIdIsNull_ShouldSaveItemWithoutRequest() {
        ItemDto itemDto = createDefaultItemDto();
        assertThat(itemDto.getRequestId(), equalTo(null));
        ItemDto savedItem = assertDoesNotThrow(() -> itemService.addNewItem(ownerId, itemDto));
        assertThat(savedItem.getId(), notNullValue());
        assertThat(savedItem.getName(), equalTo(itemDto.getName()));
        assertThat(savedItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(savedItem.getOwnerId(), equalTo(ownerId));
    }

    @Test
    void addNewItem_WhenRequestIdIsNotNull_ShouldSaveItemWithRequest() {
        ItemDto itemDto = createDefaultItemDto();
        User requester = new User();
        requester.setName("Requester Name");
        requester.setEmail("requester@example.com");
        requester = userRepository.save(requester);

        ItemRequestDto requestDto = createDefaultItemRequestDto(requester.getId());
        ItemRequest request = ItemRequestMapper.mapToItemRequest(requestDto, requester);
        // Проверка сценария где ItemRequest не найден
        Long nonExistentRequestId = 999L;
        itemDto.setRequestId(nonExistentRequestId);
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> itemService.addNewItem(ownerId, itemDto));
        assertThat(thrown.getMessage(), equalTo("ItemRequest с id = " + nonExistentRequestId + " не найден"));
        // Проверка успешного сценария
        ItemRequest savedRequest = itemRequestRepository.save(request);
        itemDto.setRequestId(savedRequest.getId());

        ItemDto savedItemDto = assertDoesNotThrow(() -> itemService.addNewItem(ownerId, itemDto));

        assertThat(savedItemDto.getId(), notNullValue());
        assertThat(savedItemDto.getName(), equalTo(itemDto.getName()));
        assertThat(savedItemDto.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(savedItemDto.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(savedItemDto.getOwnerId(), equalTo(ownerId));

        Item savedItemEntity = itemRepository.findById(savedItemDto.getId()).orElse(null);

        assertThat(savedItemEntity, notNullValue());
        assertThat(savedItemEntity.getRequest(), notNullValue());
        assertThat(savedItemEntity.getRequest().getId(), equalTo(savedRequest.getId()));
    }

    @Test
    void patchItem_WhenUserIsNull_ShouldThrowInvalidUserRequestException() {
        ItemDto itemDto = createDefaultItemDto();
        assertThrows(InvalidUserRequestException.class,
                () -> itemService.patchItem(null, itemId, itemDto));

    }


    @Test
    void patchItem_WhenItemDoesNotExistOrUserIsNotOwner_ShouldThrowNotFoundException() {
        ItemDto itemDto = createDefaultItemDto();

        assertThrows(NotFoundException.class,
                () -> itemService.patchItem(ownerId, invalidId, itemDto));

        User notOwner = createDefaultUser();
        User savedNotOwner = userRepository.save(notOwner);

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemService.patchItem(savedNotOwner.getId(), itemId, itemDto));
        assertThat(thrown.getMessage(), equalTo("Вещь с id=" + itemId + " не найдена"));
    }

    @Test
    void patchItem_WhenUserIsOwner_ShouldUpdateItemFieldsAndReturnUpdatedItem() {
        ItemDto itemDto = createDefaultItemDto();
        ItemDto patchedItemDto = assertDoesNotThrow(() -> itemService.patchItem(ownerId, itemId, itemDto));
        assertThat(patchedItemDto.getId(), notNullValue());
        assertThat(patchedItemDto.getName(), equalTo(itemDto.getName()));
        assertThat(patchedItemDto.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(patchedItemDto.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void patchItem_WhenNewItemFieldsIsEmpty_ShouldNotUpdateTheseFields() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("");
        itemDto.setDescription("");
        ItemDto patchedItemDto = assertDoesNotThrow(() -> itemService.patchItem(ownerId, itemId, itemDto));
        assertThat(patchedItemDto.getId(), notNullValue());
        assertThat(patchedItemDto.getName(), equalTo(savedItem.getName()));
        assertThat(patchedItemDto.getDescription(), equalTo(savedItem.getDescription()));
        assertThat(patchedItemDto.getAvailable(), equalTo(savedItem.getAvailable()));
    }

    @Test
    void addComment_WhenValidBooker_ShouldSaveComment() {
        User booker = createDefaultUser();
        User savedBooker = userRepository.save(booker);
        Booking booking = createDefaultBooking(savedItem, savedBooker);
        booking = bookingRepository.save(booking);
        CommentDto commentDto = createDefaultCommentDto();
        CommentDto savedCommentDto = assertDoesNotThrow(() -> itemService.addNewComment(savedBooker.getId(), itemId, commentDto));

        assertThat(savedCommentDto.getId(), notNullValue());
        assertThat(savedCommentDto.getAuthorName(), equalTo(savedBooker.getName()));
        assertThat(savedCommentDto.getText(), equalTo(commentDto.getText()));
        assertThat(savedCommentDto.getCreated(), notNullValue());

    }

    @Test
    void addComment_WhenUserIdIsNull_ShouldThrowInvalidUserRequestException() {
        CommentDto commentDto = createDefaultCommentDto();
        assertThrows(InvalidUserRequestException.class,
                () -> itemService.addNewComment(null, itemId, commentDto));
    }

    @Test
    void addComment_WhenUserDidNotBookItem_ShouldThrowValidationException() {
        CommentDto commentDto = createDefaultCommentDto();
        User invalidBooker = createDefaultUser();
        userRepository.save(invalidBooker);
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> itemService.addNewComment(invalidBooker.getId(), itemId, commentDto));
        assertThat(thrown.getMessage(), equalTo("Пользователь id=" + invalidBooker.getId() + "не бронировал вещь id=" + itemId));
    }

    @Test
    void addComment_WhenBookingIsActive_ShouldThrowValidationException() {
        CommentDto commentDto = createDefaultCommentDto();
        User booker = createDefaultUser();
        userRepository.save(booker);
        Booking invalidBooking = createDefaultBooking(savedItem, booker);
        invalidBooking.setEnd(LocalDateTime.of(2027, 1, 1, 1, 1, 1));
        bookingRepository.save(invalidBooking);
        ValidationException thrown2 = assertThrows(ValidationException.class,
                () -> itemService.addNewComment(booker.getId(), itemId, commentDto));
        assertThat(thrown2.getMessage(), equalTo("Отзыв можно оставить только после завершения бронирования"));

    }

    @Test
    void searchAvailableItemByText_WhenTextMatches_ShouldReturnMatchingItems() {
        ItemDto itemDto = createDefaultItemDto();
        User user = createDefaultUser();
        User otherOwner = userRepository.save(user);
        Item otherDto = itemRepository.save(ItemMapper.mapToItem(itemDto, otherOwner));
        List<ItemDto> items = itemService.searchAvailableItemByText("ItemDto");
        assertThat(items.size(), equalTo(1));
        assertThat(items, hasItem(hasProperty("name", equalTo(itemDto.getName()))));
    }

    @Test
    void searchAvailableItemByText_WhenTextEmpty_ShouldReturnEmptyList() {
        List<ItemDto> items = itemService.searchAvailableItemByText("");
        assertTrue(items.isEmpty());
    }


    private static CommentDto createDefaultCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Text");
        return commentDto;
    }

    public static User createDefaultUser() {
        User user = new User();
        user.setName("other User");
        user.setEmail("testOtherUser@Email.com");
        return user;
    }

    private static ItemDto createDefaultItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test ItemDto");
        itemDto.setDescription("Test ItemDto Description");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private static ItemRequestDto createDefaultItemRequestDto(Long requesterId) {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setRequesterId(requesterId);
        requestDto.setDescription("Test Request Description");
        return requestDto;
    }

    public static Booking createDefaultBooking(Item item, User booker) {
        Booking booking = new Booking();
        LocalDateTime start = LocalDateTime.of(2024, 11, 11, 14, 22, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 11, 14, 22, 0);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setBooker(booker);
        return booking;
    }

}
