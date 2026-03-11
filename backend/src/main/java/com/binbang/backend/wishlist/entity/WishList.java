package com.binbang.backend.wishlist.entity;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wishlist",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"member_id", "accommodation_id"}
        )
)
public class WishList {

    @Id
    @Column(name = "list_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;
}
