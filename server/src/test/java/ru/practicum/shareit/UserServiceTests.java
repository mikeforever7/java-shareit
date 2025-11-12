package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
public class UserServiceTests {
    private final UserService userService;
    private final UserRepository userRepository;

    private UserDto savedUserDto;
    private User savedUser;

    @BeforeEach
    void setUp() {
        String testName = "testName";
        String testEmail = "test@Email.com";

        savedUserDto = new UserDto();
        savedUserDto.setName(testName);
        savedUserDto.setEmail(testEmail);

        savedUser = userService.saveUser(savedUserDto);
        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getName(), equalTo(testName));
        assertThat(savedUser.getEmail(), equalTo(testEmail));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        UserDto newUserDto = new UserDto();
        newUserDto.setName("test Name2");
        newUserDto.setEmail("test2@Email.com");
        userService.saveUser(newUserDto);

        List<User> users = userService.getAllUsers();

        assertThat(users.size(), equalTo(2));
        assertThat(users, hasItem(hasProperty("id", equalTo(savedUser.getId()))));
        assertThat(users, hasItem(hasProperty("name", equalTo(newUserDto.getName()))));
    }

    @Test
    void getUserById_ShouldReturnUser_OrThrowNotFoundException() {
        Long existingId = savedUser.getId();
        Long nonExistingId = 999L;

        // Успешный сценарий
        UserDto retrievedUserDto = assertDoesNotThrow(() -> userService.getUserById(existingId));
        assertThat(retrievedUserDto, notNullValue());
        assertThat(retrievedUserDto.getId(), equalTo(existingId));
        assertThat(retrievedUserDto.getName(), equalTo(savedUser.getName()));
        assertThat(retrievedUserDto.getEmail(), equalTo(savedUser.getEmail()));

        //Сценарий с ошибкой, если не найден
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(nonExistingId);
        });
        assertThat(thrown.getMessage(), equalTo("Пользователь с id =" + nonExistingId + "не найден"));
    }

    @Test
    void saveUser_WhenEmailAlreadyExists_ShouldThrowEmailAlreadyExistsException() {
        UserDto newUserDto = new UserDto();
        newUserDto.setName("test Name2");
        newUserDto.setEmail("test@Email.com");
        EmailAlreadyExistsException thrown = assertThrows(EmailAlreadyExistsException.class,
                () -> {
                    userService.saveUser(newUserDto);
                });
        assertThat(thrown.getMessage(), equalTo("Пользователь с Email " + newUserDto.getEmail() + " существует"));
    }

    @Test
    void patchUser_HandlesPartialUpdates_AndValidatesEmail_AndHandlesNotFound() {
        UserDto otherUserDto = new UserDto(); // Для проверки обновления на уже существующий email
        otherUserDto.setName("test Name2");
        otherUserDto.setEmail("test2@Email.com");
        userService.saveUser(otherUserDto);

        Long userIdToUpdate = savedUser.getId();
        Long nonExistingId = 999L;

        //  Сценарий с успешным частичным обновлением (только name)
        String updatedName = "Updated Name";
        UserDto patchNameDto = new UserDto();
        patchNameDto.setName(updatedName);

        UserDto updatedUserDto1 = assertDoesNotThrow(() -> userService.patchUser(userIdToUpdate, patchNameDto));
        assertThat(updatedUserDto1.getName(), equalTo(updatedName));
        assertThat(updatedUserDto1.getEmail(), equalTo("test@Email.com"));

        //  Сценарий с успешным частичным обновлением (только email)
        String updatedEmail = "Updated Email";
        UserDto patchEmailDto = new UserDto();
        patchEmailDto.setEmail(updatedEmail);

        UserDto updatedUserDto2 = assertDoesNotThrow(() -> userService.patchUser(userIdToUpdate, patchEmailDto));
        assertThat(updatedUserDto2.getName(), equalTo(updatedName));
        assertThat(updatedUserDto2.getEmail(), equalTo(updatedEmail));

        //  Сценарий с ошибкой, если не найден
        NotFoundException thrown1 = assertThrows(NotFoundException.class, () -> {
            userService.patchUser(nonExistingId, patchNameDto);
        });
        assertThat(thrown1.getMessage(), equalTo("Пользователь с id =" + nonExistingId + "не найден"));

        //  Сценарий с ошибкой, если email существует
        UserDto userWithDuplicateEmailDto = new UserDto();
        userWithDuplicateEmailDto.setEmail("test2@Email.com");
        EmailAlreadyExistsException thrown2 = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.patchUser(userIdToUpdate, userWithDuplicateEmailDto);
        });
        assertThat(thrown2.getMessage(), equalTo("Email " + userWithDuplicateEmailDto.getEmail() + " уже занят другим пользователем"));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldNotExistInDatabase() {
        Long idForDelete = savedUser.getId();
        userService.deleteUser(idForDelete);
        boolean existsUser = userRepository.existsById(idForDelete);
        assertFalse(existsUser);
    }
}

