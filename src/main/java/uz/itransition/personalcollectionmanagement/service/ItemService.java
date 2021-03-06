package uz.itransition.personalcollectionmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.itransition.personalcollectionmanagement.entity.*;
import uz.itransition.personalcollectionmanagement.entity.enums.RoleName;
import uz.itransition.personalcollectionmanagement.projection.collection.CollectionItemsProjection;
import uz.itransition.personalcollectionmanagement.projection.item.ItemByIdProjection;
import uz.itransition.personalcollectionmanagement.projection.item.ItemProjection;
import uz.itransition.personalcollectionmanagement.repository.*;
import uz.itransition.personalcollectionmanagement.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final AuthService authService;

    private final CustomFieldService customFieldService;

    private final ItemRepository itemRepository;

    private final CustomFieldRepository customFieldRepository;

    private final TagRepository tagRepository;

    private final CollectionRepository collectionRepository;

    private final LikeRepository likeRepository;

    private final CollectionService collectionService;

    public List<ItemProjection> getLatestItems() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId() == null) return itemRepository.findLatestItems();
        else return itemRepository.findLatestItems(currentUser.getId());
    }

    public ItemByIdProjection getItemById(UUID itemId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId() == null) return itemRepository.getItemById(itemId);
        return itemRepository.getItemById(itemId, currentUser.getId());
    }

    public Page<CollectionItemsProjection> getCollectionItems(UUID collectionId,
                                                              Integer page,
                                                              String sortBy,
                                                              String sortDir) {
        int pageSize = Integer.parseInt(Constants.DEFAULT_PAGE_SIZE);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId() == null)
            return itemRepository.getItemsByCollectionId(pageable, collectionId);
        return itemRepository.getItemsByCollectionId(pageable, collectionId, currentUser.getId());
    }

    public List<CustomField> getItemCustomFields(UUID collectionId) {
        return customFieldRepository.findByCollectionId(collectionId);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getItemTags(HttpServletRequest request) {
        String[] itemTags = request.getParameterValues("itemTags");
        List<UUID> itemTagsIds = new ArrayList<>();
        for (String itemTag : itemTags) {
            itemTagsIds.add(UUID.fromString(itemTag));
        }
        return tagRepository.findAllById(itemTagsIds);
    }

    public void createItem(HttpServletRequest request, UUID collectionId) {
        User currentUser = authService.getCurrentUser();
        List<Tag> itemTags = getItemTags(request);
        Item savedItem = itemRepository.save(new Item(
                        request.getParameter("itemName").trim(),
                        itemTags,
                        currentUser,
                        collectionRepository.getById(collectionId)
                )
        );
        customFieldService.saveItemCustomFieldValues(request, savedItem);
    }

    public Page<ItemProjection> getItemsByTag(UUID tagId, Integer page) {
        int pageSize = Integer.parseInt(Constants.DEFAULT_PAGE_SIZE_GET_ALL);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId() == null)
            return itemRepository.getItemsByTag(tagId, pageable);
        else return itemRepository.getItemsByTag(tagId, pageable, currentUser.getId());
    }

    public Page<ItemProjection> getAllItems(Integer page) {
        int pageSize = Integer.parseInt(Constants.DEFAULT_PAGE_SIZE_GET_ALL);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId() == null)
            return itemRepository.getAllItems(pageable);
        else return itemRepository.getAllItems(pageable, currentUser.getId());
    }

    public void likeItem(UUID itemId) {
        User currentUser = authService.getCurrentUser();
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            if (likeRepository.existsByUserIdAndItemId(currentUser.getId(), itemId)) {
                likeRepository.deleteByUserIdAndItemId(currentUser.getId(), itemId);
            } else {
                likeRepository.save(new Like(currentUser, item.get()));
            }
        }
    }

    public ItemByIdProjection getEditingItem(UUID itemId) {
        return itemRepository.getEditingItem(itemId);
    }

    public void editItem(HttpServletRequest req) {
        Optional<Item> item = itemRepository.findById(UUID.fromString(req.getParameter("itemId")));
        List<Tag> itemTags = getItemTags(req);
        if (item.isPresent()) {
            item.get().setName(req.getParameter("itemName"));
            item.get().setTags(itemTags);
            itemRepository.save(item.get());
            customFieldService.editItemCustomFieldValues(req);
        }
    }

    public String deleteItem(UUID itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            if (isItemOwner(item.get())) {
                itemRepository.delete(item.get());
                return "redirect:/collection/id/" + item.get().getCollection().getId();
            }
        }
        return "redirect:/login";
    }

    private boolean isItemOwner(Item item) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole().getRoleName().equals(RoleName.ROLE_ADMIN)) return true;
        if (currentUser.getId().equals(item.getCreatedBy().getId())) return true;
        else return collectionService.isCollectionOwner(item.getCollection().getId());
    }
}