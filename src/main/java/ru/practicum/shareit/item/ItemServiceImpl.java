package ru.practicum.shareit.item;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.MissingUserIdHeaderException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<ItemDto> getItems(Long userId) {
        List<Item> items = itemRepository.findItemsByOwnerId(userId);
        return ItemMapper.mapToItemDtoList(items);
    }

    @Override
    public ItemWithCommentsDto getItemById(Long userId, Long itemId) {

        Item item = itemRepository.findByIdWithComments(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id =" + itemId + " не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.mapToItemWithCommentsDto(item, null, null);
        }
        Optional<Booking> lastBookingOpt = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now());
        Booking lastBooking = lastBookingOpt.orElse(null);
        Optional<Booking> nextBookingOpt = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now());
        Booking nextBooking = nextBookingOpt.orElse(null);
        return ItemMapper.mapToItemWithCommentsDto(item, lastBooking, nextBooking);
    }

    @Override
    @Transactional
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        if (userId == null) {
            throw new MissingUserIdHeaderException();
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Item item = itemRepository.save(ItemMapper.mapToItem(itemDto, owner));
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    @Transactional
    public CommentDto addNewComment(Long userId, Long itemId, CommentDto commentDto) {
        if (userId == null) {
            throw new MissingUserIdHeaderException();
        }
        Booking booking = bookingRepository.findByBookerIdAndItemId(userId, itemId)
                .orElseThrow(() -> new ValidationException("Пользователь id=" + userId + "не бронировал вещь id=" + itemId));
        if (booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Отзыв можно оставить только после завершения бронирования");
        }
        Item proxyItem = itemRepository.getReferenceById(itemId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Автор не найден"));
        Comment comment = ItemMapper.mapToComment(commentDto, proxyItem, author);
        Comment newComment = commentRepository.save(comment);
        return ItemMapper.mapToCommentDto(newComment, author);
    }

    @Override
    @Transactional
    public ItemDto patchItem(Long userId, Long itemId, ItemDto itemDto) {
        if (userId == null) {
            throw new MissingUserIdHeaderException();
        }
        Item itemForPatch = itemRepository.findByIdAndOwnerId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
        if (StringUtils.hasText(itemDto.getName())) {
            itemForPatch.setName(itemDto.getName());
        }
        if (StringUtils.hasText(itemDto.getDescription())) {
            itemForPatch.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemForPatch.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.mapToItemDto(itemRepository.save(itemForPatch));
    }

    @Override
    public List<ItemDto> searchAvailableItemByText(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String lowerCaseQuery = text.trim().toLowerCase();
        List<Item> items = itemRepository.searchAvailableItemByText(lowerCaseQuery);
        return ItemMapper.mapToItemDtoList(items);
    }

}
