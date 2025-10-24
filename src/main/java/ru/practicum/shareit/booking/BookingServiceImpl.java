package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.ReqBookingDto;
import ru.practicum.shareit.booking.dto.RespBookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.MissingUserIdHeaderException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public RespBookingDto addNewBooking(Long userId, ReqBookingDto reqBookingDto) {
        if (userId == null) {
            throw new MissingUserIdHeaderException();
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
            throw new MissingUserIdHeaderException();
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
            throw new MissingUserIdHeaderException();
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
            throw new MissingUserIdHeaderException();
        }
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingState.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingState.REJECTED);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                    LocalDateTime.now(), LocalDateTime.now());
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, LocalDateTime.now());
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, LocalDateTime.now());
            default ->
                    throw new IllegalArgumentException("Неизвестный state=" + state); //Вижу что лишнее, но лучше же оставить?
        };
        return BookingMapper.mapToBookingList(bookings);
    }
}
