package io.chanuth.clubs.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clubs")
public class RunningClub extends BaseDocument {

    @Indexed(unique = true)
    private String name;

    @Indexed(unique = true)
    private String code;

    /** No annotation: stored as a nested object inside the club document. */
    private Address address;

    /** A reference: the club document keeps only the ids of the runners. */
    @DBRef
    private Set<Runner> members = new LinkedHashSet<>();

    protected RunningClub() {
        // For Spring Data.
    }

    public RunningClub(String name, String code, Address address) {
        this.name = name;
        this.code = code;
        this.address = address;
    }

    public void update(String name, String code, Address address) {
        this.name = name;
        this.code = code;
        this.address = address;
    }

    /** Adds runners that are not members yet and returns how many were really added. */
    public int addMembers(Collection<Runner> runners) {
        Set<String> known = new LinkedHashSet<>();
        members.forEach((member) -> known.add(member.getId()));
        int added = 0;
        for (Runner runner : runners) {
            if (known.add(runner.getId())) {
                members.add(runner);
                added++;
            }
        }
        return added;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Address getAddress() {
        return address;
    }

    public Set<Runner> getMembers() {
        return members;
    }
}
