package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("select ir from ItemRequest ir left join fetch ir.items where ir.requester.id = :userId " +
            "order by ir.created desc")
    List<ItemRequest> findUserRequestWithAnswers(long userId);

    List<ItemRequest> findAllByOrderByCreatedDesc();
}
