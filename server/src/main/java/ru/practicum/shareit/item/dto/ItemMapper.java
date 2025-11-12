package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static ItemDto mapToItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setOwnerId(item.getOwner().getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static ItemForItemRequestDto mapToItemForRequestDto(Item item) {
        return new ItemForItemRequestDto(item.getId(), item.getOwner().getId(), item.getName());
    }

    public static ItemWithCommentsDto mapToItemWithCommentsDto(Item item, Booking lastBooking, Booking nextBooking) {
        List<CommentDto> commentDtos = item.getComments().stream()
                .map(comment -> mapToCommentDto(comment, comment.getAuthor()))
                .toList();
        BookingShortDto lastShortBooking = BookingMapper.mapToBookingShortDto(lastBooking);
        BookingShortDto nextShortBooking = BookingMapper.mapToBookingShortDto(nextBooking);
        return new ItemWithCommentsDto(item.getId(), item.getOwner().getId(),
                item.getName(), item.getDescription(), item.getAvailable(), commentDtos, lastShortBooking, nextShortBooking
        );
    }

    public static Item mapToItem(ItemDto itemDto, User owner) {
        Item item = new Item();
        item.setOwner(owner);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());

        return item;
    }

    public static List<ItemDto> mapToItemDtoList(List<Item> items) {
        return items.stream().map(ItemMapper::mapToItemDto).toList();
    }

    public static List<ItemForItemRequestDto> mapToItemForRequestDtoList(List<Item> items) {
        return items.stream().map(ItemMapper::mapToItemForRequestDto).toList();
    }

    public static Comment mapToComment(CommentDto commentDto, Item item, User user) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        return comment;
    }

    public static CommentDto mapToCommentDto(Comment comment, User author) {
        return new CommentDto(comment.getId(), comment.getText(), author.getName(), comment.getCreated());
    }
}