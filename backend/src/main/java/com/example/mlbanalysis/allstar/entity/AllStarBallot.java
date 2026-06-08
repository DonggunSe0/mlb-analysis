package com.example.mlbanalysis.allstar.entity;

import com.example.mlbanalysis.auth.entity.AuthUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "all_star_ballots",
        uniqueConstraints = @UniqueConstraint(name = "uk_all_star_ballot_user_vote_date", columnNames = {"user_id", "vote_date"})
)
public class AllStarBallot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(name = "vote_date", nullable = false)
    private LocalDate voteDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "ballot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AllStarSelection> selections = new ArrayList<>();

    protected AllStarBallot() {
    }

    public AllStarBallot(AuthUser user, LocalDate voteDate, Instant createdAt) {
        this.user = user;
        this.voteDate = voteDate;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public AuthUser getUser() { return user; }
    public LocalDate getVoteDate() { return voteDate; }
    public Instant getCreatedAt() { return createdAt; }
    public List<AllStarSelection> getSelections() { return selections; }

    public void addSelection(AllStarSelection selection) {
        selection.setBallot(this);
        selections.add(selection);
    }
}
