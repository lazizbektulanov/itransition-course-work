package uz.itransition.personalcollectionmanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.itransition.personalcollectionmanagement.entity.User;
import uz.itransition.personalcollectionmanagement.projection.user.ProfileProjection;
import uz.itransition.personalcollectionmanagement.projection.user.UserAccountProjection;
import uz.itransition.personalcollectionmanagement.projection.user.UserProjection;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(nativeQuery = true,
    value = "select " +
            "cast(u.id as varchar) as id," +
            "u.full_name as fullName," +
            "u.email as email," +
            "u.profile_img_url as profileImgUrl," +
            "u.bio as bio," +
            "(select count(c.id) from collections c " +
            "where c.owner_id=u.id) as collectionsNumber," +
            "(select count(i.id) from items i " +
            "where i.created_by_id=u.id) as itemsNumber " +
            "from users u " +
            "where u.id=:userId")
    ProfileProjection getUserProfile(UUID userId);

    @Query(nativeQuery = true,
    value = "select " +
            "cast(u.id as varchar) as id," +
            "u.full_name as fullName," +
            "u.email as email," +
            "u.is_active as isActive," +
            "u.profile_img_url as profileImgUrl," +
            "u.last_login_time as lastLoginTime, " +
            "r.role_name as role " +
            "from users u " +
            "join roles r on u.role_id = r.id")
    Page<UserProjection> getAllUsers(Pageable pageable);

    @Query(nativeQuery = true,
    value = "select " +
            "cast(u.id as varchar) as id," +
            "u.full_name as fullName," +
            "u.bio as bio," +
            "u.profile_img_url as profileImgUrl " +
            "from users u " +
            "where u.id=:userId")
    UserAccountProjection getUserAccountInfo(UUID userId);
}
