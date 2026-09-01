package library.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Member {

    private final String id;
    private final String name;
    private final Membership membership;
    private BigDecimal fineBalance;

    public Member(String id, String name, Membership membership) {
        this.id = id;
        this.name = name;
        this.membership = membership;
        this.fineBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Membership getMembership() {
        return membership;
    }

    public BigDecimal getFineBalance() {
        return fineBalance;
    }
    public void addFine(BigDecimal amount) {
        this.fineBalance = this.fineBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public void pay(BigDecimal amount) {
        this.fineBalance = this.fineBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return id.equals(member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{id='" + id + "', name='" + name + "', membership=" + membership
                + ", fineBalance=" + fineBalance + "}";
    }
}
