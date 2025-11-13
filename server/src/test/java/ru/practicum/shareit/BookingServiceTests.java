package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.RequestState;
import ru.practicum.shareit.booking.dto.ReqBookingDto;
import ru.practicum.shareit.booking.dto.RespBookingDto;

import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTests {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User booker;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setName("Owner Name");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker Name");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("A test item");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void addNewBooking_WhenValidData_ShouldReturnSavedBooking() {
        Long userId = booker.getId();
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());

        RespBookingDto savedBooking = assertDoesNotThrow(() -> bookingService.addNewBooking(userId, reqDto));

        assertThat(savedBooking.getId(), notNullValue());
        assertThat(savedBooking.getStart(), equalTo(reqDto.getStart()));
        assertThat(savedBooking.getEnd(), equalTo(reqDto.getEnd()));
        assertThat(savedBooking.getStatus(), equalTo(BookingState.WAITING)); // Статус по умолчанию
        assertThat(savedBooking.getBooker().getId(), equalTo(booker.getId()));
        assertThat(savedBooking.getItem().getId(), equalTo(item.getId()));
    }

    @Test
    void addNewBooking_WhenUserIdIsNull_ShouldThrowInvalidUserRequestException() {

        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());

        assertThrows(InvalidUserRequestException.class, () -> bookingService.addNewBooking(null, reqDto));
    }

    @Test
    void addNewBooking_WhenItemNotAvailable_ShouldThrowValidationException() {

        Long userId = booker.getId();
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());

        item.setAvailable(false);
        itemRepository.save(item);

        ValidationException thrown = assertThrows(ValidationException.class, () -> bookingService.addNewBooking(userId, reqDto));
        assertThat(thrown.getMessage(), containsString("не доступна"));
    }

    @Test
    void addNewBooking_WhenStartIsInPast_ShouldThrowValidationException() {
        Long userId = booker.getId();
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        reqDto.setStart(LocalDateTime.now().minusHours(2));
        reqDto.setEnd(LocalDateTime.now().minusHours(1));

        ValidationException thrown = assertThrows(ValidationException.class, () -> bookingService.addNewBooking(userId, reqDto));
        assertThat(thrown.getMessage(), containsString("не может быть в прошлом"));
    }

    @Test
    void patchBooking_WhenApprovedByOwner_ShouldChangeStatusToApproved() {
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        RespBookingDto initialBooking = bookingService.addNewBooking(booker.getId(), reqDto);
        Long bookingId = initialBooking.getId();
        Long ownerId = owner.getId();

        Boolean approved = true;

        RespBookingDto patchedBooking = assertDoesNotThrow(() -> bookingService.patchBooking(ownerId, bookingId, approved));

        assertThat(patchedBooking.getStatus(), equalTo(BookingState.APPROVED));

        Item itemInDb = itemRepository.findById(item.getId()).orElse(null);
        assertThat(itemInDb, notNullValue());
        assertThat(itemInDb.getAvailable(), is(false));
    }

    @Test
    void patchBooking_WhenRejectedByOwner_ShouldChangeStatusToRejected() {
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        RespBookingDto initialBooking = bookingService.addNewBooking(booker.getId(), reqDto);
        Long bookingId = initialBooking.getId();
        Long ownerId = owner.getId();

        Boolean approved = false;

        RespBookingDto patchedBooking = assertDoesNotThrow(() -> bookingService.patchBooking(ownerId, bookingId, approved));

        assertThat(patchedBooking.getStatus(), equalTo(BookingState.REJECTED));

        Item itemInDb = itemRepository.findById(item.getId()).orElse(null);
        assertThat(itemInDb, notNullValue());
        assertThat(itemInDb.getAvailable(), is(true));

    }

    @Test
    void patchBooking_WhenUserIsNotOwner_ShouldThrowForbiddenException() {
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        RespBookingDto initialBooking = bookingService.addNewBooking(booker.getId(), reqDto);
        Long bookingId = initialBooking.getId();
        Long notOwnerId = booker.getId();
        Boolean approved = true;

        assertThrows(ForbiddenException.class, () -> bookingService.patchBooking(notOwnerId, bookingId, approved));
    }

    @Test
    void getBooking_WhenUserIsBooker_ShouldReturnBooking() {
        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        RespBookingDto savedBooking = bookingService.addNewBooking(booker.getId(), reqDto);
        Long bookingId = savedBooking.getId();
        Long bookerId = booker.getId();

        RespBookingDto retrievedBooking = assertDoesNotThrow(() -> bookingService.getBooking(bookerId, bookingId));

        assertThat(retrievedBooking.getId(), equalTo(bookingId));
    }

    @Test
    void getBooking_WhenUserIsOwner_ShouldReturnBooking() {

        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        RespBookingDto savedBooking = bookingService.addNewBooking(booker.getId(), reqDto);
        Long bookingId = savedBooking.getId();
        Long ownerId = owner.getId();
        RespBookingDto retrievedBooking = assertDoesNotThrow(() -> bookingService.getBooking(ownerId, bookingId));
        assertThat(retrievedBooking.getId(), equalTo(bookingId));
    }

    @Test
    void getBooking_WhenUserIsNeitherBookerNorOwner_ShouldThrowForbiddenException() {
        User thirdUser = new User();
        thirdUser.setName("Third User");
        thirdUser.setEmail("third@example.com");
        thirdUser = userRepository.save(thirdUser);

        ReqBookingDto reqDto = createDefaultBookingDto(item.getId());
        RespBookingDto savedBooking = bookingService.addNewBooking(booker.getId(), reqDto);
        Long bookingId = savedBooking.getId();
        Long thirdUserId = thirdUser.getId();

        assertThrows(ForbiddenException.class, () -> bookingService.getBooking(thirdUserId, bookingId));
    }

    @Test
    void getBookingsByUserAndState_WhenStateIsAll_ShouldReturnAllBookingsForUser() {

        ReqBookingDto reqDto1 = createDefaultBookingDto(item.getId());
        bookingService.addNewBooking(booker.getId(), reqDto1);

        Item anotherItem = new Item();
        anotherItem.setName("Another Item");
        anotherItem.setDescription("Another item");
        anotherItem.setAvailable(true);
        anotherItem.setOwner(owner);
        Item savedAnotherItem = itemRepository.save(anotherItem);

        ReqBookingDto reqDto2 = createDefaultBookingDto(savedAnotherItem.getId());
        bookingService.addNewBooking(booker.getId(), reqDto2);

        Long bookerId = booker.getId();
        RequestState state = RequestState.ALL;

        List<RespBookingDto> bookings = assertDoesNotThrow(() -> bookingService.getBookingsByUserAndState(bookerId, state));

        assertThat(bookings, hasSize(2));

    }

    @Test
    void getBookingsByOwnerAndState_WhenStateIsAll_ShouldReturnBookingsForOwnerItems() {

        ReqBookingDto reqDto1 = createDefaultBookingDto(item.getId());
        bookingService.addNewBooking(booker.getId(), reqDto1);


        User anotherBooker = new User();
        anotherBooker.setName("Another Booker");
        anotherBooker.setEmail("another_booker@example.com");
        anotherBooker = userRepository.save(anotherBooker);

        ReqBookingDto reqDto2 = createDefaultBookingDto(item.getId());
        bookingService.addNewBooking(anotherBooker.getId(), reqDto2);

        Long ownerId = owner.getId();
        RequestState state = RequestState.ALL;


        List<RespBookingDto> bookings = assertDoesNotThrow(() -> bookingService.getBookingsByOwnerAndState(ownerId, state));

        assertThat(bookings, hasSize(2));
    }

    private ReqBookingDto createDefaultBookingDto(Long itemId) {
        ReqBookingDto dto = new ReqBookingDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));
        return dto;
    }
}