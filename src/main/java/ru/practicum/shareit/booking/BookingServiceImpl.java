package ru.practicum.shareit.booking;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.ReqBookingDto;
import ru.practicum.shareit.booking.dto.RespBookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.strategy.*;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.InvalidUserRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final Map<RequestState, BookingStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    private void initializeStrategyMap() {
        strategyMap.put(RequestState.ALL, new AllBookingStrategy(bookingRepository));
        strategyMap.put(RequestState.CURRENT, new CurrentBookingStrategy(bookingRepository));
        strategyMap.put(RequestState.FUTURE, new FutureBookingStrategy(bookingRepository));
        strategyMap.put(RequestState.PAST, new PastBookingStrategy(bookingRepository));
        strategyMap.put(RequestState.REJECTED, new RejectedBookingStrategy(bookingRepository));
        strategyMap.put(RequestState.WAITING, new WaitingBookingStrategy(bookingRepository));
    }

    @Override
    @Transactional
    public RespBookingDto addNewBooking(Long userId, ReqBookingDto reqBookingDto) {
        if (userId == null) {
            throw new InvalidUserRequestException();
        }
        if (reqBookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата бронирования не может быть в прошлом");
        }
        if (reqBookingDto.getEnd().isBefore(reqBookingDto.getStart()) || reqBookingDto.getEnd().isEqual(reqBookingDto.getStart())) {
            throw new ValidationException("Дата завершения бронирования не может быть раньше либо равно дате начала бронирования");
        }
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Item item = itemRepository.findItemById(reqBookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + reqBookingDto.getItemId() + " не найдена"));
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с id=" + reqBookingDto.getItemId() + " не доступна для бронирования");
        }
        Booking booking = BookingMapper.mapToBooking(reqBookingDto, booker, item);
        bookingRepository.save(booking);
        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    @Transactional
    public RespBookingDto patchBooking(Long userId, Long bookingId, Boolean approved) {
        if (userId == null) {
            throw new InvalidUserRequestException();
        }
        Booking bookingForPatch = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));
        if (!bookingForPatch.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь не совпадает с владельцем вещи");
        }
        if (approved) {
            bookingForPatch.setStatus(BookingState.APPROVED);
            bookingForPatch.getItem().setAvailable(false);
        } else {
            bookingForPatch.setStatus(BookingState.REJECTED);
        }
        bookingRepository.save(bookingForPatch);
        return BookingMapper.mapToBookingDto(bookingForPatch);
    }

    @Override
    public RespBookingDto getBooking(Long userId, Long bookingId) {
        if (userId == null) {
            throw new InvalidUserRequestException();
        }
        Booking bookingForGet = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));
        if (!bookingForGet.getBooker().getId().equals(userId) &&
                !bookingForGet.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Просмаривать бронирование может только booker и owner");
        }
        return BookingMapper.mapToBookingDto(bookingForGet);
    }

    @Override
    public List<RespBookingDto> getBookingsByUserAndState(Long userId, RequestState state) {
        if (userId == null) {
            throw new InvalidUserRequestException();
        }
        BookingStrategy strategy = strategyMap.get(state);
        if (strategy == null) {
            throw new IllegalArgumentException("Неизвестный state=" + state);
        }
        List<Booking> bookings = strategy.getBookings(userId);
        return BookingMapper.mapToBookingList(bookings);
    }
}
